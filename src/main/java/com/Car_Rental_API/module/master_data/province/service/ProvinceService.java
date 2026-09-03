package com.Car_Rental_API.module.master_data.province.service;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceRequest;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceResponse;
import com.Car_Rental_API.module.master_data.province.mapper.ProvinceMapper;
import com.Car_Rental_API.module.master_data.province.model.Province;
import com.Car_Rental_API.module.master_data.province.repository.ProvinceRepository;
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
public class ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    // * Query & List Operations
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAllProvincesFallback")
    @Cacheable(value = "provinces", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<ProvinceResponse> getAllProvinces(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> provinceRepository.countAll(filter));
        List<ProvinceResponse> list = provinceMapper.toResponses(provinceRepository.findAll(filter));
        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getProvinceByIdFallback")
    @Cacheable(value = "province", key = "#id")
    public Province getProvinceById(Long id) {
        return provinceRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Province not found", 404));
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getProvinceResponseByIdFallback")
    public ProvinceResponse getProvinceResponseById(Long id) {
        return provinceMapper.toResponse(getProvinceById(id));
    }

    // * Mutation Operations
    @Transactional
    @CacheEvict(value = {"provinces", "province", "dropdown_provinces"}, allEntries = true)
    public void createProvince(ProvinceRequest request, Long userId) {
        Province province = provinceMapper.fromCreateRequest(request);
        province.setCreated(LocalDateTime.now());
        province.setCreatedBy(userId);
        provinceRepository.save(province);
    }

    @Transactional
    @CacheEvict(value = {"provinces", "province", "dropdown_provinces"}, allEntries = true)
    public void updateProvince(Long id, ProvinceRequest request, Long userId) {
        Province province = getProvinceById(id);
        provinceMapper.updateFromRequest(request, province);
        province.setModified(LocalDateTime.now());
        province.setModifiedBy(userId);
        provinceRepository.update(province);
    }

    @Transactional
    @CacheEvict(value = {"provinces", "province", "dropdown_provinces"}, allEntries = true)
    public void deleteProvince(Long id) {
        int affected = provinceRepository.deleteById(id);
        if (affected == 0) {
            throw new GlobalException("Province not found", 404);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return provinceRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_provinces")
    public List<DropdownResponse> getDropdown() {
        return provinceRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private PageResult<ProvinceResponse> getAllProvincesFallback(BaseFilterRequest req, Throwable t) {
        log.error("Circuit breaker open for getAllProvinces — cause: {}", t.getMessage(), t);
        throw new GlobalException("Province service temporarily unavailable", 503);
    }

    private Province getProvinceByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getProvinceById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Province service temporarily unavailable", 503);
    }

    private ProvinceResponse getProvinceResponseByIdFallback(Long id, Throwable t) {
        log.error("Circuit breaker open for getProvinceResponseById(id={}) — cause: {}", id, t.getMessage(), t);
        throw new GlobalException("Province service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Province service temporarily unavailable", 503);
    }
}
