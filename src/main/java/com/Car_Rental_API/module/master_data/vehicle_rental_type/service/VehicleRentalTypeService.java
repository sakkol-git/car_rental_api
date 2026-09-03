package com.Car_Rental_API.module.master_data.vehicle_rental_type.service;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import static java.time.LocalDateTime.now;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.module.master_data.vehicle_category.service.VehicleCategoryService;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.VehicleRentalType;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleRentalTypeService {

	private final VehicleRentalTypeRepository rentalTypeRepository;
	private final VehicleRentalTypeMapper rentalTypeMapper;
	private final VehicleCategoryService categoryService;

	// * Cached Count for Infinite Scroll & Pagination
	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicleRentalTypes_count", key = "#req != null ? #req.toString() : 'default'")
	public long countRentalTypes(VehicleRentalTypeFilterRequest req) {
		return rentalTypeRepository.countAll(req != null ? req : new VehicleRentalTypeFilterRequest());
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicleRentalTypes", key = "#req != null ? #req.toString() : 'default'")
	public PageResult<VehicleRentalTypeResponse> getAllRentalTypes(VehicleRentalTypeFilterRequest req) {
		VehicleRentalTypeFilterRequest filter = req != null ? req : new VehicleRentalTypeFilterRequest();
		List<VehicleRentalTypeResponse> list = rentalTypeRepository.findAll(filter).stream()
				.map(rentalTypeMapper::toResponse)
				.toList();
		return new PageResult<>(list, countRentalTypes(filter));
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicleRentalType", key = "#id")
	public VehicleRentalType getRentalTypeById(Long id) {
		return rentalTypeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Vehicle Rental Type not found"));
	}

	@CircuitBreaker(name = "defaultService")
	public VehicleRentalTypeResponse getRentalTypeResponseById(Long id) {
		return rentalTypeMapper.toResponse(getRentalTypeById(id));
	}

	@Transactional
	@CacheEvict(value = {"vehicleRentalTypes", "vehicleRentalTypes_count", "vehicleRentalType", "dropdown_vehicleRentalTypes"}, allEntries = true)
	public void createRentalType(VehicleRentalTypeRequest request, Long userId) {
		categoryService.getCategoryById(request.getCategoryId());

		VehicleRentalType rentalType = rentalTypeMapper.fromCreateRequest(request);
		rentalType.setCreated(now());
		rentalType.setCreatedBy(userId);

		rentalTypeRepository.save(rentalType);
	}

	@Transactional
	@CacheEvict(value = {"vehicleRentalTypes", "vehicleRentalTypes_count", "vehicleRentalType", "dropdown_vehicleRentalTypes",
			"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void updateRentalType(Long id, VehicleRentalTypeRequest request, Long userId) {
		categoryService.getCategoryById(request.getCategoryId());

		VehicleRentalType rentalType = getRentalTypeById(id);
		rentalTypeMapper.updateFromRequest(request, rentalType);
		rentalType.setModified(now());
		rentalType.setModifiedBy(userId);

		rentalTypeRepository.update(rentalType);
	}

	@Transactional
	@CacheEvict(value = {"vehicleRentalTypes", "vehicleRentalTypes_count", "vehicleRentalType", "dropdown_vehicleRentalTypes",
			"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void deleteRentalType(Long id) {
		getRentalTypeById(id);
		rentalTypeRepository.deleteById(id);
	}

	// * Dropdown Operations
	public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
		return rentalTypeRepository.findDropdown(req);
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "dropdown_vehicleRentalTypes")
	public List<DropdownResponse> getDropdown() {
		return rentalTypeRepository.findDropdown();
	}
}
