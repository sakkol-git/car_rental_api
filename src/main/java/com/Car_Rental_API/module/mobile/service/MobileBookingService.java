package com.Car_Rental_API.module.mobile.service;






import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationFilterRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyVehiclePriceResponse;
import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.module.report.customer_review.repository.CustomerReviewRepository;
import com.Car_Rental_API.module.report.customer_review.mapper.CustomerReviewMapper;
import static com.db_access.jooq.tables.CustomerReviews.CUSTOMER_REVIEWS;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.DropdownWithPriceResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.module.master_data.journey_price.repository.JourneyPriceRepository;

import com.Car_Rental_API.module.master_data.sub_location.mapper.SubLocationMapper;
import com.Car_Rental_API.module.master_data.sub_location.repository.SubLocationRepository;

import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationResponse;
import com.Car_Rental_API.module.master_data.vechicle.service.VehicleService;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleResponse;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.VehicleBrandService;
import com.Car_Rental_API.module.master_data.vehicle_category.service.VehicleCategoryService;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryResponse;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.VehicleRentalTypeService;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeResponse;
import com.Car_Rental_API.module.mobile.dto.response.MobileFilterOptionsResponse;
import com.Car_Rental_API.module.mobile.dto.request.MobileSubLocationFilterRequest;
import com.Car_Rental_API.module.mobile.dto.response.MobileVehicleDetailResponse;
import com.Car_Rental_API.module.mobile.dto.request.MobileVehicleFilterRequest;
import com.Car_Rental_API.module.mobile.dto.response.TimeSlotResponse;
import com.Car_Rental_API.module.mobile.dto.response.VehicleScheduleResponse;


import com.Car_Rental_API.module.report.customer_review.dto.request.CustomerReviewFilterRequest;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MobileBookingService {

	private final VehicleRentalTypeService rentalTypeService;
	private final VehicleService vehicleService;
	private final VehicleCategoryService vehicleCategoryService;
	private final VehicleBrandService vehicleBrandService;
	private final CustomerReviewRepository customerReviewRepository;
	private final CustomerReviewMapper customerReviewMapper;
	private final SubLocationRepository subLocationRepository;
	private final SubLocationMapper subLocationMapper;
	private final JourneyPriceRepository journeyPriceRepository;
	private final DSLContext dsl;

	// * Destinations / Sub Locations listing with journey route pricing for Mobile App
	public List<SubLocationResponse> getMobileSubLocations(MobileSubLocationFilterRequest filterReq) {
		MobileSubLocationFilterRequest filter = filterReq != null ? filterReq : new MobileSubLocationFilterRequest();

		Long fromPId = filter.getFromProvinceId();
		Long toPId = filter.getToProvinceId();
		Byte journeyType = filter.getJourneyType();

		// * Route price record (all price types)
		JourneyVehiclePriceResponse routePriceRecord =
				(fromPId != null && toPId != null)
				? journeyPriceRepository.findJourneyPriceRecord(fromPId, toPId, filter.getVehicleModelId())
				: null;

		SubLocationFilterRequest subFilter = new SubLocationFilterRequest();
		subFilter.setProvinceId(toPId);
		subFilter.setIsPublic((byte) 1);
		subFilter.setPage(1);
		subFilter.setSize(500);

		return subLocationRepository.findAll(subFilter).stream()
				.map(sub -> {
					SubLocationResponse res = subLocationMapper.toResponse(sub);

					if (routePriceRecord != null) {
						// Populate all price fields from route
						res.setOneWayPrice(routePriceRecord.getOneWayPrice());
						res.setOneDayTourPrice(routePriceRecord.getOneDayTourPrice());
						res.setRoundTripPrice(routePriceRecord.getRoundTripPrice());
						res.setMultiCityPrice(routePriceRecord.getMultiCityPrice());
						res.setCityTourPrice(routePriceRecord.getCityTourPrice());
						res.setPricePerDay(routePriceRecord.getCityTourPrice());

						// Determine highlighted price based on journeyType
						BigDecimal selectedPrice = null;
						if (journeyType != null) {
							selectedPrice = switch (journeyType) {
								case 1 -> routePriceRecord.getOneWayPrice();
								case 2 -> routePriceRecord.getOneDayTourPrice();
								case 3 -> routePriceRecord.getRoundTripPrice();
								case 4 -> routePriceRecord.getMultiCityPrice();
								case 5 -> routePriceRecord.getPricePerDay();
								default -> routePriceRecord.getOneWayPrice();
							};
						}
						// Override defaultPrice with route price as the main display price
						if (selectedPrice != null && selectedPrice.compareTo(BigDecimal.ZERO) > 0) {
							res.setDefaultPrice(selectedPrice);
							res.setPrice(selectedPrice);
						} else {
							res.setPrice(sub.getDefaultPrice());
						}
					} else {
						// No route price found — use sub-location's own default_price
						res.setPrice(sub.getDefaultPrice());
					}

					return res;
				})
				.toList();
	}

	// * Journey Origin & Destination Provinces Dropdowns
	public List<DropdownResponse> getJourneyFromProvinces() {
		return journeyPriceRepository.findJourneyFromProvinces();
	}

	public List<DropdownWithPriceResponse> getJourneyToProvincesWithPrice(
			Long fromProvinceId, Long vehicleModelId, Byte journeyType) {
		return journeyPriceRepository.findJourneyToProvincesWithPrice(fromProvinceId, vehicleModelId, journeyType);
	}

	// * Rental Categories (Screen 1)
	public List<VehicleRentalTypeResponse> getRentalTypes(VehicleRentalTypeFilterRequest filter) {
		VehicleRentalTypeFilterRequest req = filter != null ? filter : new VehicleRentalTypeFilterRequest();
		if (req.getPage() <= 0) req.setPage(1);
		if (req.getSize() <= 0) req.setSize(100);
		return rentalTypeService.getAllRentalTypes(req).data();
	}

	// * Vehicle Categories / Rental Services with Image & Description (Screen 1)
	public List<VehicleCategoryResponse> getCategories() {
		BaseFilterRequest filter = new BaseFilterRequest();
		filter.setPage(1);
		filter.setSize(100);
		return vehicleCategoryService.getAllCategories(filter).data();
	}

	// * Filter Options (Screen 3)
	public MobileFilterOptionsResponse getFilterOptions() {
		return MobileFilterOptionsResponse.builder()
				.passengers(vehicleService.getPassengerCapacities())
				.ratings(List.of(1, 2, 3, 4, 5))
				.categories(vehicleCategoryService.getDropdown())
				.rentalTypes(rentalTypeService.getDropdown())
				.brands(vehicleBrandService.getDropdown())
				.build();
	}

	// * Time Slots Dropdown (Overloaded for no args - defaults to all available for selection)
	public List<TimeSlotResponse> getTimeSlots() {
		return getTimeSlots(false);
	}

	// * Time Slots Dropdown with boolean flag to mark all busy/disabled (e.g. for Vehicle Schedule response)
	public List<TimeSlotResponse> getTimeSlots(boolean markAllBusy) {
		List<String> times = List.of(
				"06:00 AM", "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
				"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
				"12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
				"03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM",
				"06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM"
		);
		List<TimeSlotResponse> list = new ArrayList<>();
		for (String time : times) {
			list.add(new TimeSlotResponse(time, markAllBusy, markAllBusy));
		}
		return list;
	}

	// * Vehicle Schedule Availability (Available & Unavailable Dates for Calendar UI + Busy Time Slots)
	public VehicleScheduleResponse getVehicleSchedule(Long vehicleId, Integer year, Integer month) {
		VehicleResponse base = vehicleService.getVehicleResponseById(vehicleId);
		if (base.getIsPublic() != null && base.getIsPublic() == 0L) {
			throw new RuntimeException("Vehicle not found or not available");
		}

		// * Total units available for this vehicle (default to 1 if not set)
		int vehicleQuantity = (base.getQuantity() != null && base.getQuantity() > 0) ? base.getQuantity() : 1;

		LocalDate today = LocalDate.now();
		int y = (year != null && year > 2000) ? year : today.getYear();
		int m = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();

		YearMonth ym = YearMonth.of(y, m);
		LocalDate startOfMonth = ym.atDay(1);
		LocalDate endOfMonth = ym.atEndOfMonth();

		// * Fetch all active bookings overlapping this month, including how many units each booking occupies
		var records = dsl.select(SALES_ORDERS.START_DATE, SALES_ORDERS.END_DATE, SALES_ORDERS.AMOUNT_OF_VEHICLES)
				.from(SALES_ORDERS)
				.where(SALES_ORDERS.VEHICLE_ID.eq(vehicleId)
						.and(SALES_ORDERS.IS_ACTIVE.eq((byte) 1))
						.and(SALES_ORDERS.ORDER_STATUS.notIn((byte) 4, (byte) 5))
						.and(SALES_ORDERS.START_DATE.lessOrEqual(endOfMonth))
						.and(SALES_ORDERS.END_DATE.greaterOrEqual(startOfMonth)))
				.fetch();

		List<String> availableList = new ArrayList<>();
		List<String> unavailableList = new ArrayList<>();

		for (int d = 1; d <= ym.lengthOfMonth(); d++) {
			LocalDate date = ym.atDay(d);
			String dateStr = date.toString();

			// * Count total booked units across all orders that cover this specific date
			int bookedUnitsOnDate = 0;
			for (var rec : records) {
				LocalDate start = rec.get(SALES_ORDERS.START_DATE);
				LocalDate end = rec.get(SALES_ORDERS.END_DATE);
				if (start != null && end != null && !date.isBefore(start) && !date.isAfter(end)) {
					Integer units = rec.get(SALES_ORDERS.AMOUNT_OF_VEHICLES);
					bookedUnitsOnDate += (units != null && units > 0) ? units : 1;
				}
			}

			// * A date is only unavailable when ALL units of the vehicle are fully booked on that day
			if (bookedUnitsOnDate >= vehicleQuantity) {
				unavailableList.add(dateStr);
			} else {
				availableList.add(dateStr);
			}
		}

		return VehicleScheduleResponse.builder()
				.vehicleId(vehicleId)
				.monthYear(ym.getMonth().name() + " " + ym.getYear())
				.availableDates(availableList)
				.unavailableDates(unavailableList)
				.timeSlots(getTimeSlots(false))
				.build();
	}

	// * Vehicle Listings (Screen 2)
	public PageResult<VehicleResponse> getMobileVehicles(MobileVehicleFilterRequest req) {
		MobileVehicleFilterRequest filter = req != null ? req : new MobileVehicleFilterRequest();
		filter.setIsPublic((byte) 1);
		return vehicleService.getAllVehicles(filter);
	}

	// * Vehicle Details & Rating Breakdown (Screen 4)
	public MobileVehicleDetailResponse getVehicleDetail(Long vehicleId) {
		VehicleResponse base = vehicleService.getVehicleResponseById(vehicleId);
		if (base.getIsPublic() != null && base.getIsPublic() == 0L) {
			throw new RuntimeException("Vehicle not found or not available");
		}

		int totalReviews = countReviews(vehicleId, null);
		BigDecimal rawRating = dsl.select(org.jooq.impl.DSL.avg(CUSTOMER_REVIEWS.RATING_STARS))
				.from(CUSTOMER_REVIEWS)
				.where(CUSTOMER_REVIEWS.VEHICLE_ID.eq(vehicleId).and(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1)).and(CUSTOMER_REVIEWS.IS_DISABLED.eq((byte) 0)))
				.fetchOne(0, BigDecimal.class);

		double avgRating = (rawRating == null) ? 4.5 : Math.round(rawRating.doubleValue() * 10.0) / 10.0;

		Map<Integer, Long> ratingBreakdown = new HashMap<>();
		for (int i = 1; i <= 5; i++) {
			ratingBreakdown.put(i, (long) countReviews(vehicleId, i));
		}

		CustomerReviewFilterRequest reviewFilter = new CustomerReviewFilterRequest();
		reviewFilter.setPage(1);
		reviewFilter.setSize(5);

		return MobileVehicleDetailResponse.builder()
				.id(base.getId())
				.brandId(base.getBrandId())
				.brandName(base.getBrandName())
				.modelId(base.getModelId())
				.modelName(base.getModelName())
				.nameKh(base.getNameKh())
				.nameEn(base.getNameEn())
				.nameZh(base.getNameZh())
				.vehicleCode(base.getVehicleCode())
				.plateNumber(base.getPlateNumber())
				.fileName(base.getFileName())
				.fileUrl(base.getFileUrl())
				.quantity(base.getQuantity())
				.passengers(base.getPassengers())
				.averageRating(avgRating)
				.totalReviews(totalReviews)
				.ratingBreakdown(ratingBreakdown)
				.categories(base.getCategories())
				.rentalTypes(base.getRentalTypes())
				.slides(base.getSlides())
				.facilities(base.getFacilities())
				.items(base.getItems())
				.recentReviews(getVehicleReviews(vehicleId, reviewFilter).data())
				.build();
	}

	// * Paginated Vehicle Reviews Listing (Screen 4 - Infinite Scroll & Pagination)
	public PageResult<CustomerReviewResponse> getVehicleReviews(Long vehicleId, CustomerReviewFilterRequest req) {
		CustomerReviewFilterRequest filter = req != null ? req : new CustomerReviewFilterRequest();
		filter.setVehicleId(vehicleId);
		if (filter.getPage() < 1) {
			filter.setPage(1);
		}
		if (filter.getSize() < 1) {
			filter.setSize(10);
		}

		List<CustomerReviewResponse> list = customerReviewRepository.findAll(filter).stream()
				.map(customerReviewMapper::toResponse)
				.toList();
		long total = customerReviewRepository.countAll(filter);

		return new PageResult<>(list, total);
	}

	// * Helper to Count Reviews (Total or per Star)
	private int countReviews(Long vehicleId, Integer star) {
		var cond = CUSTOMER_REVIEWS.VEHICLE_ID.eq(vehicleId).and(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1)).and(CUSTOMER_REVIEWS.IS_DISABLED.eq((byte) 0));
		if (star != null) {
			cond = cond.and(CUSTOMER_REVIEWS.RATING_STARS.eq(BigDecimal.valueOf(star)));
		}
		return dsl.selectCount().from(CUSTOMER_REVIEWS).where(cond).fetchOne(0, int.class);
	}
}
