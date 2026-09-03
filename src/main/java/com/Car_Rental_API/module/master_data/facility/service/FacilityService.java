package com.Car_Rental_API.module.master_data.facility.service;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityRequest;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityResponse;
import com.Car_Rental_API.module.master_data.facility.mapper.FacilityMapper;
import com.Car_Rental_API.module.master_data.facility.model.Facility;
import com.Car_Rental_API.module.master_data.facility.repository.FacilityRepository;
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
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    // * Query & List Operations
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAllFacilitiesFallback")
    @Cacheable(value = "facilities", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<FacilityResponse> getAllFacilities(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> facilityRepository.countAll(filter));
        List<FacilityResponse> list = facilityMapper.toResponses(facilityRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getFacilityByIdFallback")
    @Cacheable(value = "facility", key = "#id")
    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Facility not found", 404));
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getFacilityResponseByIdFallback")
    public FacilityResponse getFacilityResponseById(Long id) {
        return facilityMapper.toResponse(getFacilityById(id));
    }

    // * Mutation Operations
    @Transactional
    @CacheEvict(value = {"facilities", "facility", "dropdown_facilities"}, allEntries = true)
    public void createFacility(FacilityRequest request, Long userId) {
        Facility facility = facilityMapper.fromCreateRequest(request);
        facility.setCreated(LocalDateTime.now());
        facility.setCreatedBy(userId);
        facilityRepository.save(facility);
    }

    @Transactional
    @CacheEvict(value = {"facilities", "facility", "dropdown_facilities"}, allEntries = true)
    public void updateFacility(Long id, FacilityRequest request, Long userId) {
        Facility facility = getFacilityById(id);
        facilityMapper.updateFromRequest(request, facility);
        facility.setModified(LocalDateTime.now());
        facility.setModifiedBy(userId);
        facilityRepository.update(facility);
    }

    @Transactional
    @CacheEvict(value = {"facilities", "facility", "dropdown_facilities"}, allEntries = true)
    public void deleteFacility(Long id) {
        int affected = facilityRepository.deleteById(id);
        if (affected == 0) {
            throw new GlobalException("Facility not found", 404);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return facilityRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_facilities")
    public List<DropdownResponse> getDropdown() {
        return facilityRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private PageResult<FacilityResponse> getAllFacilitiesFallback(BaseFilterRequest req, Throwable t) {
        log.error("Circuit breaker open for getAllFacilities — cause: {}", t.getMessage(), t);
        throw new GlobalException("Facility service temporarily unavailable", 503);
    }

    private Facility getFacilityByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getFacilityById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Facility service temporarily unavailable", 503);
    }

    private FacilityResponse getFacilityResponseByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getFacilityResponseById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Facility service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Facility service temporarily unavailable", 503);
    }
}
