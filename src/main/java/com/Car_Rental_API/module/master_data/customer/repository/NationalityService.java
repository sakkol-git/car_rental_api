package com.Car_Rental_API.module.master_data.customer.repository;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityRequest;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityResponse;
import com.Car_Rental_API.module.master_data.nationality.mapper.NationalityMapper;
import com.Car_Rental_API.module.master_data.nationality.model.Nationality;
import com.Car_Rental_API.module.master_data.nationality.repository.NationalityRepository;
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
public class NationalityService {

    private final NationalityRepository nationalityRepository;
    private final NationalityMapper nationalityMapper;

    // * Query & List Operations
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAllNationalitiesFallback")
    @Cacheable(value = "nationalities", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<NationalityResponse> getAllNationalities(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> nationalityRepository.countAll(filter));
        List<NationalityResponse> list = nationalityMapper.toResponses(nationalityRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getNationalityByIdFallback")
    @Cacheable(value = "nationality", key = "#id")
    public Nationality getNationalityById(Long id) {
        return nationalityRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Nationality not found", 404));
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getNationalityResponseByIdFallback")
    public NationalityResponse getNationalityResponseById(Long id) {
        return nationalityMapper.toResponse(getNationalityById(id));
    }

    // * Mutation Operations
    @Transactional
    @CacheEvict(value = { "nationalities", "nationality", "dropdown_nationalities" }, allEntries = true)
    public void createNationality(NationalityRequest request, Long userId) {
        Nationality nationality = nationalityMapper.fromCreateRequest(request);
        nationality.setCreated(LocalDateTime.now());
        nationality.setCreatedBy(userId);
        nationalityRepository.save(nationality);
    }

    @Transactional
    @CacheEvict(value = { "nationalities", "nationality", "dropdown_nationalities" }, allEntries = true)
    public void updateNationality(Long id, NationalityRequest request, Long userId) {
        Nationality nationality = getNationalityById(id);
        nationalityMapper.updateFromRequest(request, nationality);
        nationality.setModified(LocalDateTime.now());
        nationality.setModifiedBy(userId);
        nationalityRepository.update(nationality);
    }

    @Transactional
    @CacheEvict(value = { "nationalities", "nationality", "dropdown_nationalities" }, allEntries = true)
    public void deleteNationality(Long id) {
        int affected = nationalityRepository.deleteById(id);
        if (affected == 0) {
            throw new GlobalException("Nationality not found", 404);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return nationalityRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_nationalities")
    public List<DropdownResponse> getDropdown() {
        return nationalityRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private PageResult<NationalityResponse> getAllNationalitiesFallback(BaseFilterRequest req, Throwable t) {
        log.error("Circuit breaker open for getAllNationalities — cause: {}", t.getMessage(), t);
        throw new GlobalException("Nationality service temporarily unavailable", 503);
    }

    private Nationality getNationalityByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getNationalityById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Nationality service temporarily unavailable", 503);
    }

    private NationalityResponse getNationalityResponseByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getNationalityResponseById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Nationality service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Nationality service temporarily unavailable", 503);
    }
}
