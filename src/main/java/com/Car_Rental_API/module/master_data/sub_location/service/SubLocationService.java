package com.Car_Rental_API.module.master_data.sub_location.service;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.province.service.ProvinceService;
import com.Car_Rental_API.module.master_data.sub_location.model.SubLocation;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationFilterRequest;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationRequest;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationResponse;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationStatusRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class SubLocationService {

    private final SubLocationRepository subLocationRepository;
    private final SubLocationMapper subLocationMapper;
    private final ProvinceService provinceService;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "subLocations", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<SubLocationResponse> getAllSubLocations(SubLocationFilterRequest req) {
        SubLocationFilterRequest filter = req != null ? req : new SubLocationFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> subLocationRepository.countAll(filter));

        List<SubLocationResponse> list = subLocationRepository.findAll(filter).stream()
                .map(subLocationMapper::toResponse)
                .toList();

        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "subLocation", key = "#id")
    public SubLocation getSubLocationById(Long id) {
        return subLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub Location not found"));
    }

    @CircuitBreaker(name = "defaultService")
    public SubLocationResponse getSubLocationResponseById(Long id) {
        return subLocationMapper.toResponse(getSubLocationById(id));
    }

    @Transactional
    @CacheEvict(value = {"subLocations", "subLocation", "dropdown_subLocations"}, allEntries = true)
    public void createSubLocation(SubLocationRequest request, Long userId) {
        provinceService.getProvinceById(request.getProvinceId());

        SubLocation subLocation = subLocationMapper.fromCreateRequest(request);
        subLocation.setCreated(now());
        subLocation.setCreatedBy(userId);

        subLocationRepository.save(subLocation);
    }

    @Transactional
    @CacheEvict(value = {"subLocations", "subLocation", "dropdown_subLocations"}, allEntries = true)
    public void updateSubLocation(Long id, SubLocationRequest request, Long userId) {
        provinceService.getProvinceById(request.getProvinceId());

        SubLocation subLocation = getSubLocationById(id);
        subLocationMapper.updateFromRequest(request, subLocation);
        subLocation.setModified(now());
        subLocation.setModifiedBy(userId);

        subLocationRepository.update(subLocation);
    }

    @Transactional
    @CacheEvict(value = {"subLocations", "subLocation", "dropdown_subLocations"}, allEntries = true)
    public void deleteSubLocation(Long id) {
        getSubLocationById(id);
        subLocationRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = {"subLocations", "subLocation", "dropdown_subLocations"}, allEntries = true)
    public void updateStatus(Long id, SubLocationStatusRequest request, Long userId) {
        getSubLocationById(id);
        subLocationRepository.updateStatus(id, request.getIsPublic(), userId);
    }
    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return subLocationRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_subLocations")
    public List<DropdownResponse> getDropdown() {
        return subLocationRepository.findDropdown();
    }
}
