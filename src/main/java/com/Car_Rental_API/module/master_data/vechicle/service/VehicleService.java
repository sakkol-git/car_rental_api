package com.Car_Rental_API.module.master_data.vechicle.service;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import static java.time.LocalDateTime.now;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.vechicle.model.Vehicle;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleCategoryItemResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFacilityRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFacilityResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFilterRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleItemRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleItemResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleRentalTypeItemResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleSlideResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleStatusRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final VehicleMapper vehicleMapper;

	// * Cached Count for Infinite Scroll & Pagination
	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicles_count", key = "#req != null ? #req.toString() : 'default'")
	public long countVehicles(VehicleFilterRequest req) {
		return vehicleRepository.countAll(req != null ? req : new VehicleFilterRequest());
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicles", key = "#req != null ? #req.toString() : 'default'")
	public PageResult<VehicleResponse> getAllVehicles(VehicleFilterRequest req) {
		VehicleFilterRequest filter = req != null ? req : new VehicleFilterRequest();
		long total = QueryUtil.shouldCount(filter.getPage(), () -> vehicleRepository.countAll(filter));
		return new PageResult<>(toResponses(vehicleRepository.findAll(filter)), total);
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "vehicle", key = "#id")
	public Vehicle getVehicleById(Long id) {
		return vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Vehicle not found"));
	}

	@CircuitBreaker(name = "defaultService")
	public VehicleResponse getVehicleResponseById(Long id) {
		return toResponses(List.of(getVehicleById(id))).get(0);
	}

	// * CRUD Operations
	@Transactional
	@CacheEvict(value = {"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void createVehicle(VehicleRequest request, Long userId) {
		validateRequest(request);
		Vehicle vehicle = vehicleMapper.fromCreateRequest(request);
		vehicle.setCreated(now());
		vehicle.setCreatedBy(userId);

		Vehicle saved = vehicleRepository.save(vehicle);
		replaceChildren(saved.getId(), request, userId);
	}

	@Transactional
	@CacheEvict(value = {"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void updateVehicle(Long id, VehicleRequest request, Long userId) {
		validateRequest(request);
		Vehicle vehicle = getVehicleById(id);
		vehicleMapper.updateFromRequest(request, vehicle);
		vehicle.setModified(now());
		vehicle.setModifiedBy(userId);

		vehicleRepository.update(vehicle);
		replaceChildren(id, request, userId);
	}

	@Transactional
	@CacheEvict(value = {"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void deleteVehicle(Long id) {
		getVehicleById(id);
		vehicleRepository.deleteById(id);
	}

	@Transactional
	@CacheEvict(value = {"vehicles", "vehicles_count", "vehicle", "dropdown_vehicles"}, allEntries = true)
	public void updateStatus(Long id, VehicleStatusRequest request, Long userId) {
		getVehicleById(id);
		vehicleRepository.updateStatus(id, request.getIsPublic(), userId);
	}

	// * Response Mapping & Children Sync
	private List<VehicleResponse> toResponses(List<Vehicle> vehicles) {
		if (vehicles.isEmpty()) {
			return List.of();
		}
		List<VehicleResponse> responses = vehicleMapper.toResponses(vehicles);
		List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).toList();

		Map<Long, List<VehicleCategoryItemResponse>> categories = vehicleRepository.findCategoriesByVehicleIds(vehicleIds);
		Map<Long, List<VehicleRentalTypeItemResponse>> rentalTypes = vehicleRepository.findRentalTypesByVehicleIds(vehicleIds);
		Map<Long, List<VehicleSlideResponse>> slides = vehicleRepository.findSlidesByVehicleIds(vehicleIds);
		Map<Long, List<VehicleFacilityResponse>> facilities = vehicleRepository.findFacilitiesByVehicleIds(vehicleIds);
		Map<Long, List<VehicleItemResponse>> items = vehicleRepository.findItemsByVehicleIds(vehicleIds);
		Map<Long, Double> ratings = vehicleRepository.findRatingsByVehicleIds(vehicleIds);

		responses.forEach(response -> {
			Long vehicleId = response.getId();
			response.setCategories(categories.getOrDefault(vehicleId, List.of()));
			response.setRentalTypes(rentalTypes.getOrDefault(vehicleId, List.of()));
			response.setSlides(slides.getOrDefault(vehicleId, List.of()));
			response.setFacilities(facilities.getOrDefault(vehicleId, List.of()));
			response.setItems(items.getOrDefault(vehicleId, List.of()));
			Double avgRating = ratings.getOrDefault(vehicleId, 4.5);
			response.setAverageRating(avgRating);
		});
		return responses;
	}

	private void replaceChildren(Long vehicleId, VehicleRequest request, Long userId) {
		vehicleRepository.replaceCategoryMappings(vehicleId, safeList(request.getCategoryIds()).stream().distinct().toList());
		vehicleRepository.replaceRentalTypeMappings(vehicleId, safeList(request.getRentalTypeIds()).stream().distinct().toList());
		vehicleRepository.replaceSlides(vehicleId, safeList(request.getSlides()));
		vehicleRepository.replaceFacilities(vehicleId, safeList(request.getFacilities()));
		vehicleRepository.replaceItems(vehicleId, safeList(request.getItems()), userId);
	}

	// * Request Validation
	private void validateRequest(VehicleRequest request) {
		if (!vehicleRepository.existsActiveBrand(request.getBrandId())) {
			throw new RuntimeException("Vehicle Brand not found");
		}
		if (!vehicleRepository.existsActiveModelForBrand(request.getModelId(), request.getBrandId())) {
			throw new RuntimeException("Vehicle Model not found for selected brand");
		}

		Set<Long> categoryIds = new HashSet<>(safeList(request.getCategoryIds()));
		if (categoryIds.size() != vehicleRepository.countActiveCategories(categoryIds)) {
			throw new RuntimeException("Vehicle Category not found");
		}

		Set<Long> rentalTypeIds = new HashSet<>(safeList(request.getRentalTypeIds()));
		if (rentalTypeIds.size() != vehicleRepository.countActiveRentalTypes(rentalTypeIds)) {
			throw new RuntimeException("Vehicle Rental Type not found");
		}

		Set<Long> facilityIds = new HashSet<>(safeList(request.getFacilities()).stream().map(VehicleFacilityRequest::getFacilityId).filter(Objects::nonNull).toList());
		if (facilityIds.size() != vehicleRepository.countActiveFacilities(facilityIds)) {
			throw new RuntimeException("Facility not found");
		}

		List<VehicleItemRequest> items = safeList(request.getItems());
		if (!items.isEmpty() && items.size() != request.getQuantity()) {
			throw new RuntimeException("Vehicle items must match vehicle quantity");
		}
	}

	private <T> List<T> safeList(List<T> values) {
		return Optional.ofNullable(values).orElse(List.of());
	}

	// * Dropdown Operations
	public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
		return vehicleRepository.findDropdown(req);
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "dropdown_vehicles")
	public List<DropdownResponse> getDropdown() {
		return vehicleRepository.findDropdown();
	}

	@CircuitBreaker(name = "defaultService")
	@Cacheable(value = "passenger_capacities")
	public List<Integer> getPassengerCapacities() {
		return vehicleRepository.findDistinctPassengerCapacities();
	}
}
