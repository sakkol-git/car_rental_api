package com.Car_Rental_API.module.master_data.customer_support.service;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportRequest;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportResponse;
import com.Car_Rental_API.module.master_data.customer_support.mapper.CustomerSupportMapper;
import com.Car_Rental_API.module.master_data.customer_support.model.CustomerSupport;
import com.Car_Rental_API.module.master_data.customer_support.repository.CustomerSupportRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSupportService {

    private final CustomerSupportRepository customerSupportRepository;
    private final CustomerSupportMapper customerSupportMapper;

    // * Query & List Operations
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAllCustomerSupportsFallback")
    @Cacheable(value = "customerSupports", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<CustomerSupportResponse> getAllCustomerSupports(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> customerSupportRepository.countAll(filter));
        List<CustomerSupportResponse> list = customerSupportMapper.toResponses(customerSupportRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getCustomerSupportByIdFallback")
    @Cacheable(value = "customerSupport", key = "#id")
    public CustomerSupport getCustomerSupportById(Long id) {
        return customerSupportRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Customer Support not found", 404));
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getCustomerSupportResponseByIdFallback")
    public CustomerSupportResponse getCustomerSupportResponseById(Long id) {
        return customerSupportMapper.toResponse(getCustomerSupportById(id));
    }

    // * Mutation Operations
    @Transactional
    @CacheEvict(value = {"customerSupports", "customerSupport", "dropdown_customerSupports"}, allEntries = true)
    public void createCustomerSupport(CustomerSupportRequest request, Long userId) {
        CustomerSupport customerSupport = customerSupportMapper.fromCreateRequest(request);
        customerSupport.setCreated(LocalDateTime.now());
        customerSupport.setCreatedBy(userId);
        customerSupportRepository.save(customerSupport);
    }

    @Transactional
    @CacheEvict(value = {"customerSupports", "customerSupport", "dropdown_customerSupports"}, allEntries = true)
    public void updateCustomerSupport(Long id, CustomerSupportRequest request, Long userId) {
        CustomerSupport customerSupport = getCustomerSupportById(id);
        customerSupportMapper.updateFromRequest(request, customerSupport);
        customerSupport.setModified(LocalDateTime.now());
        customerSupport.setModifiedBy(userId);
        customerSupportRepository.update(customerSupport);
    }

    @Transactional
    @CacheEvict(value = {"customerSupports", "customerSupport", "dropdown_customerSupports"}, allEntries = true)
    public void deleteCustomerSupport(Long id) {
        int affected = customerSupportRepository.deleteById(id);
        if (affected == 0) {
            throw new GlobalException("Customer Support not found", 404);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return customerSupportRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_customerSupports")
    public List<DropdownResponse> getDropdown() {
        return customerSupportRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private PageResult<CustomerSupportResponse> getAllCustomerSupportsFallback(BaseFilterRequest req, Throwable t) {
        log.error("Circuit breaker open for getAllCustomerSupports — cause: {}", t.getMessage(), t);
        throw new GlobalException("Customer Support service temporarily unavailable", 503);
    }

    private CustomerSupport getCustomerSupportByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getCustomerSupportById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Customer Support service temporarily unavailable", 503);
    }

    private CustomerSupportResponse getCustomerSupportResponseByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getCustomerSupportResponseById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Customer Support service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Customer Support service temporarily unavailable", 503);
    }
}
