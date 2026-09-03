package com.Car_Rental_API.module.master_data.customer.service;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerRequest;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerResponse;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerFilterRequest;
import com.Car_Rental_API.module.master_data.customer.mapper.CustomerMapper;
import com.Car_Rental_API.module.master_data.customer.model.Customer;
import com.Car_Rental_API.module.master_data.customer.repository.CustomerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // * Query & List Operations
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAllCustomersFallback")
    @Cacheable(value = "customers", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<CustomerResponse> getAllCustomers(CustomerFilterRequest req) {
        CustomerFilterRequest filter = req != null ? req : new CustomerFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> customerRepository.countAll(filter));
        List<CustomerResponse> list = customerMapper.toResponses(customerRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getCustomerByIdFallback")
    @Cacheable(value = "customer", key = "#id")
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Customer not found", 404));
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getCustomerResponseByIdFallback")
    public CustomerResponse getCustomerResponseById(Long id) {
        return customerMapper.toResponse(getCustomerById(id));
    }

    // * Mutation Operations
    @Transactional
    @CacheEvict(value = {"customers", "customer", "dropdown_customers"}, allEntries = true)
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Pre-check with EXISTS semantics (fast, no full row fetch)
        if (customerRepository.existsByPhone(request.getPhoneNumber())) {
            throw new GlobalException("Customer phone number already exists", 409);
        }
        Customer customer = customerMapper.fromCreateRequest(request);
        customer.setCreated(LocalDateTime.now());

        try {
            return customerMapper.toResponse(customerRepository.save(customer));
        } catch (DuplicateKeyException e) {
            // DB unique constraint caught the race — convert to user-friendly error
            throw new GlobalException("Customer phone number already exists", 409);
        }
    }

    // * Read-only Customer ID Lookup (No DB mutation)
    public Long findExistingCustomerId(Long id, String phone, String email) {
        if (id != null) {
            var existing = customerRepository.findById(id);
            if (existing.isPresent()) return existing.get().getId();
        }
        if (phone != null && !phone.isBlank()) {
            var existingPhone = customerRepository.findByPhone(phone.trim());
            if (existingPhone.isPresent()) return existingPhone.get().getId();
        }
        if (email != null && !email.isBlank()) {
            var existingEmail = customerRepository.findByEmail(email.trim());
            if (existingEmail.isPresent()) return existingEmail.get().getId();
        }
        return null;
    }

    // * Get or Sync Customer Record
    @Transactional
    @CacheEvict(value = {"customers", "customer", "dropdown_customers"}, allEntries = true)
    public Customer getOrCreateCustomer(String fullName, String phone, String email, Byte osType) {
        return getOrCreateCustomer(null, fullName, phone, email, osType);
    }

    @Transactional
    @CacheEvict(value = {"customers", "customer", "dropdown_customers"}, allEntries = true)
    public Customer getOrCreateCustomer(Long id, String fullName, String phone, String email, Byte osType) {
        if (id != null) {
            var existing = customerRepository.findById(id);
            if (existing.isPresent()) {
                Customer c = existing.get();
                if (fullName != null && !fullName.isBlank()) c.setFullName(fullName.trim());
                if (phone != null && !phone.isBlank()) c.setPhoneNumber(phone.trim());
                if (email != null && !email.isBlank()) c.setEmail(email.trim());
                customerRepository.update(c);
                return c;
            }
        }
        if (phone != null && !phone.isBlank()) {
            var existingPhone = customerRepository.findByPhone(phone.trim());
            if (existingPhone.isPresent()) return existingPhone.get();
        }
        if (email != null && !email.isBlank()) {
            var existingEmail = customerRepository.findByEmail(email.trim());
            if (existingEmail.isPresent()) return existingEmail.get();
        }

        Customer c = new Customer();
        c.setId(id);
        c.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : "App Customer");
        c.setPhoneNumber(phone != null ? phone.trim() : "");
        c.setEmail(email != null ? email.trim() : null);
        c.setOsType(osType != null ? osType : (byte) 1);
        c.setLanguage((byte) 1);
        c.setIsVerified((byte) 1);
        c.setIsActive(1);
        c.setCreated(LocalDateTime.now());

        // Use upsert when ID is provided (handles race if same ID inserted concurrently)
        return id != null ? customerRepository.upsert(c) : customerRepository.save(c);
    }

    @Transactional
    @CacheEvict(value = {"customers", "customer", "dropdown_customers"}, allEntries = true)
    public void updateCustomer(Long id, CustomerRequest request) {
        Customer customer = getCustomerById(id);
        customerMapper.updateFromRequest(request, customer);
        customer.setModified(LocalDateTime.now());
        customerRepository.update(customer);
    }

    @Transactional
    @CacheEvict(value = {"customers", "customer", "dropdown_customers"}, allEntries = true)
    public void deleteCustomer(Long id) {
        int affected = customerRepository.deleteById(id);
        if (affected == 0) {
            throw new GlobalException("Customer not found", 404);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return customerRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_customers")
    public List<DropdownResponse> getDropdown() {
        return customerRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    // Each fallback logs the original exception cause and throws a
    // meaningful GlobalException so errors are never silently swallowed.

    private PageResult<CustomerResponse> getAllCustomersFallback(CustomerFilterRequest req, Throwable t) {
        log.error("Circuit breaker open for getAllCustomers — cause: {}", t.getMessage(), t);
        throw new GlobalException("Customer service temporarily unavailable", 503);
    }

    private Customer getCustomerByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getCustomerById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Customer service temporarily unavailable", 503);
    }

    private CustomerResponse getCustomerResponseByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getCustomerResponseById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Customer service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Customer service temporarily unavailable", 503);
    }
}
