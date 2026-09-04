package com.Car_Rental_API.module.mobile.controller;




import com.Car_Rental_API.module.mobile.service.MobileBookingService;
import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFacilityResponse;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.DropdownWithPriceResponse;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleResponse;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryResponse;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeResponse;
import com.Car_Rental_API.module.mobile.dto.response.MobileFilterOptionsResponse;
import com.Car_Rental_API.module.mobile.dto.request.MobileSubLocationFilterRequest;
import com.Car_Rental_API.module.mobile.dto.response.MobileVehicleDetailResponse;
import com.Car_Rental_API.module.mobile.dto.request.MobileVehicleFilterRequest;
import com.Car_Rental_API.module.mobile.dto.response.VehicleScheduleResponse;
import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewFilterRequest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@Tag(name = "25. Booking Catalog", description = "Public Catalog APIs for Vehicle Listings, Route Sub-Locations, Filters & Vehicle Availability (Used by Web/Admin & Mobile App)")
public class MobileCatalogController extends BaseController {

	private final MobileBookingService mobileBookingService;

	// * Sub Locations / Destinations (Trip Information Screen)
	@GetMapping("/sub-locations")
	@Operation(summary = "Get active public sub locations / destinations with default or route prices", description = "journeyType: 1=One-Way, 2=One-Day-Tour, 3=Round-Trip, 4=Multi-City")
	public ResponseEntity<BaseResponse<List<SubLocationResponse>>> getSubLocations(@Valid MobileSubLocationFilterRequest filter) {
		return success(mobileBookingService.getMobileSubLocations(filter));
	}

	// * Journey Origin Provinces Dropdown (Origin provinces that exist in active journey prices)
	@GetMapping("/origin-provinces")
	@Operation(summary = "Get origin provinces dropdown with configured journey prices")
	public ResponseEntity<BaseResponse<List<DropdownResponse>>> getJourneyOriginProvinces() {
		return success(mobileBookingService.getJourneyFromProvinces());
	}

	// * Journey Destination Provinces with route price (filtered by fromProvinceId, vehicleModelId, journeyType)
	@GetMapping("/destination-provinces")
	@Operation(summary = "Get destination provinces with route price per journeyType", description = "journeyType: 1=One-Way, 2=One-Day-Tour, 3=Round-Trip, 4=Multi-City, 5=City Tour")
	public ResponseEntity<BaseResponse<List<DropdownWithPriceResponse>>> getJourneyDestinationProvinces(@RequestParam Long fromProvinceId, @RequestParam Long vehicleModelId, @RequestParam Byte journeyType) {
		return success(mobileBookingService.getJourneyToProvincesWithPrice(fromProvinceId, vehicleModelId, journeyType));
	}

	// * Rental Categories / Rental Services with Image & Description (Screen 1)
	@GetMapping("/rental-services")
	@Operation(summary = "Get vehicle rental categories / services with Image & Description (Travel Rental, Logistics Rental)")
	public ResponseEntity<BaseResponse<List<VehicleCategoryResponse>>> getCategories() {
		return success(mobileBookingService.getCategories());
	}

	// * Rental Categories (Screen 1)
	@GetMapping("/rental-types")
	@Operation(summary = "Get vehicle rental categories / sub-types (filtered by categoryId)")
	public ResponseEntity<BaseResponse<List<VehicleRentalTypeResponse>>> getRentalTypes(@Valid VehicleRentalTypeFilterRequest filter) {
		return success(mobileBookingService.getRentalTypes(filter));
	}

	// * Filter Options (Screen 3)
	@GetMapping("/filter-options")
	@Operation(summary = "Get filter options (Passenger capacities, Ratings, Categories, Brands)")
	public ResponseEntity<BaseResponse<MobileFilterOptionsResponse>> getFilterOptions() {
		return success(mobileBookingService.getFilterOptions());
	}

	// * Vehicle Listings (Screen 2 - Infinite Scroll)
	@GetMapping("/vehicles")
	@Operation(summary = "Search & list vehicles with filters and infinite scroll")
	public ResponseEntity<BaseResponse<List<VehicleResponse>>> getMobileVehicles(@Valid MobileVehicleFilterRequest filter) {
		MobileVehicleFilterRequest req = filter != null ? filter : new MobileVehicleFilterRequest();
		return successPage(mobileBookingService.getMobileVehicles(req), req);
	}

	// * Vehicle Details (Screen 4)
	@GetMapping("/vehicles/{id}")
	@Operation(summary = "Get full vehicle details (slides, facilities, rating breakdown)")
	public ResponseEntity<BaseResponse<MobileVehicleDetailResponse>> getVehicleDetail(@PathVariable Long id) {
		return success(mobileBookingService.getVehicleDetail(id));
	}

	// * Vehicle Availability Schedule (Available & Booked Dates Calendar UI + Time Slots)
	@GetMapping("/vehicles/{id}/schedule")
	@Operation(summary = "Get vehicle availability schedule calendar (Available/Booked dates & time slots)")
	public ResponseEntity<BaseResponse<VehicleScheduleResponse>> getVehicleSchedule(@PathVariable Long id, @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
		return success(mobileBookingService.getVehicleSchedule(id, year, month));
	}

	// * Vehicle Reviews (Screen 4 - Paginated / Infinite Scroll)
	@GetMapping("/vehicles/{id}/reviews")
	@Operation(summary = "Get paginated customer reviews list for a specific vehicle")
	public ResponseEntity<BaseResponse<List<CustomerReviewResponse>>> getVehicleReviews(@PathVariable Long id, @Valid CustomerReviewFilterRequest filter) {
		CustomerReviewFilterRequest req = filter != null ? filter : new CustomerReviewFilterRequest();
		return successPage(mobileBookingService.getVehicleReviews(id, req), req);
	}
}
