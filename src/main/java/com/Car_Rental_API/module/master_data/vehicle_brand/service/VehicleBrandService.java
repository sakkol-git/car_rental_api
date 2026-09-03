package com.Car_Rental_API.module.master_data.vehicle_brand.service;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.VehicleBrand;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandRequest;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class VehicleBrandService {

    private final VehicleBrandRepository brandRepository;
    private final VehicleBrandMapper brandMapper;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleBrands", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<VehicleBrandResponse> getAllBrands(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> brandRepository.countAll(filter));

        List<VehicleBrandResponse> list = brandRepository.findAll(filter).stream()
                .map(brandMapper::toResponse)
                .toList();

        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleBrand", key = "#id")
    public VehicleBrand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle Brand not found"));
    }

    @CircuitBreaker(name = "defaultService")
    public VehicleBrandResponse getBrandResponseById(Long id) {
        return brandMapper.toResponse(getBrandById(id));
    }

    @Transactional
    @CacheEvict(value = {"vehicleBrands", "vehicleBrand", "dropdown_vehicleBrands"}, allEntries = true)
    public void createBrand(VehicleBrandRequest request, Long userId) {
        VehicleBrand brand = brandMapper.fromCreateRequest(request);
        brand.setCreated(now());
        brand.setCreatedBy(userId);

        brandRepository.save(brand);
    }

    @Transactional
    @CacheEvict(value = {"vehicleBrands", "vehicleBrand", "dropdown_vehicleBrands",
            "vehicleModels", "vehicleModel", "dropdown_vehicleModels",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void updateBrand(Long id, VehicleBrandRequest request, Long userId) {
        VehicleBrand brand = getBrandById(id);
        brandMapper.updateFromRequest(request, brand);
        brand.setModified(now());
        brand.setModifiedBy(userId);

        brandRepository.update(brand);
    }

    @Transactional
    @CacheEvict(value = {"vehicleBrands", "vehicleBrand", "dropdown_vehicleBrands",
            "vehicleModels", "vehicleModel", "dropdown_vehicleModels",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void deleteBrand(Long id) {
        getBrandById(id);
        brandRepository.deleteById(id);
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return brandRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_vehicleBrands")
    public List<DropdownResponse> getDropdown() {
        return brandRepository.findDropdown();
    }
}
