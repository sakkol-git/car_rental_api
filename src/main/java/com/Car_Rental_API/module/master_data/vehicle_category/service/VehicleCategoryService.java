package com.Car_Rental_API.module.master_data.vehicle_category.service;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.vehicle_category.model.VehicleCategory;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryRequest;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;
    private final VehicleCategoryMapper categoryMapper;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleCategories", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<VehicleCategoryResponse> getAllCategories(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> categoryRepository.countAll(filter));

        List<VehicleCategoryResponse> list = categoryRepository.findAll(filter).stream()
                .map(categoryMapper::toResponse)
                .toList();

        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleCategory", key = "#id")
    public VehicleCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle Category not found"));
    }

    @CircuitBreaker(name = "defaultService")
    public VehicleCategoryResponse getCategoryResponseById(Long id) {
        return categoryMapper.toResponse(getCategoryById(id));
    }

    @Transactional
    @CacheEvict(value = {"vehicleCategories", "vehicleCategory", "dropdown_vehicleCategories"}, allEntries = true)
    public void createCategory(VehicleCategoryRequest request, Long userId) {
        VehicleCategory category = categoryMapper.fromCreateRequest(request);
        category.setCreated(now());
        category.setCreatedBy(userId);

        categoryRepository.save(category);
    }

    @Transactional
    @CacheEvict(value = {"vehicleCategories", "vehicleCategory", "dropdown_vehicleCategories",
            "vehicleRentalTypes", "vehicleRentalTypes_count", "vehicleRentalType", "dropdown_vehicleRentalTypes",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void updateCategory(Long id, VehicleCategoryRequest request, Long userId) {
        VehicleCategory category = getCategoryById(id);
        categoryMapper.updateFromRequest(request, category);
        category.setModified(now());
        category.setModifiedBy(userId);

        categoryRepository.update(category);
    }

    @Transactional
    @CacheEvict(value = {"vehicleCategories", "vehicleCategory", "dropdown_vehicleCategories",
            "vehicleRentalTypes", "vehicleRentalTypes_count", "vehicleRentalType", "dropdown_vehicleRentalTypes",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void deleteCategory(Long id) {
        getCategoryById(id);
        categoryRepository.deleteById(id);
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return categoryRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_vehicleCategories")
    public List<DropdownResponse> getDropdown() {
        return categoryRepository.findDropdown();
    }
}
