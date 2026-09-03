package com.Car_Rental_API.module.master_data.vehicle_model.service;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.vehicle_model.model.VehicleModel;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelRequest;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelResponse;
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
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleModelMapper modelMapper;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleModels", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<VehicleModelResponse> getAllModels(VehicleModelFilterRequest req) {
        VehicleModelFilterRequest filter = req != null ? req : new VehicleModelFilterRequest();
        long total = QueryUtil.shouldCount(filter.getPage(), () -> modelRepository.countAll(filter));

        List<VehicleModelResponse> list = modelRepository.findAll(filter).stream()
                .map(modelMapper::toResponse)
                .toList();

        return new PageResult<>(list, total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "vehicleModel", key = "#id")
    public VehicleModel getModelById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle Model not found"));
    }

    @CircuitBreaker(name = "defaultService")
    public VehicleModelResponse getModelResponseById(Long id) {
        return modelMapper.toResponse(getModelById(id));
    }

    @Transactional
    @CacheEvict(value = {"vehicleModels", "vehicleModel", "dropdown_vehicleModels"}, allEntries = true)
    public void createModel(VehicleModelRequest request, Long userId) {
        validateBrand(request.getBrandId());

        VehicleModel model = modelMapper.fromCreateRequest(request);
        model.setCreated(now());
        model.setCreatedBy(userId);

        modelRepository.save(model);
    }

    @Transactional
    @CacheEvict(value = {"vehicleModels", "vehicleModel", "dropdown_vehicleModels",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void updateModel(Long id, VehicleModelRequest request, Long userId) {
        validateBrand(request.getBrandId());

        VehicleModel model = getModelById(id);
        modelMapper.updateFromRequest(request, model);
        model.setModified(now());
        model.setModifiedBy(userId);

        modelRepository.update(model);
    }

    @Transactional
    @CacheEvict(value = {"vehicleModels", "vehicleModel", "dropdown_vehicleModels",
            "vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
    public void deleteModel(Long id) {
        getModelById(id);
        modelRepository.deleteById(id);
    }

    // * Ensure selected brand exists before saving model data.
    private void validateBrand(Long brandId) {
        if (!modelRepository.existsActiveBrand(brandId)) {
            throw new RuntimeException("Vehicle Brand not found");
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return modelRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_vehicleModels")
    public List<DropdownResponse> getDropdown() {
        return modelRepository.findDropdown();
    }
}
