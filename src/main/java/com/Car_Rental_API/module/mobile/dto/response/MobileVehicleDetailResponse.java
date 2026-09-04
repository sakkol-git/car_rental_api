package com.Car_Rental_API.module.mobile.dto.response;





import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleCategoryItemResponse;
import com.Car_Rental_API.module.report.customer_review.dto.response.CustomerReviewResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleRentalTypeItemResponse;

import java.util.List;
import java.util.Map;


import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFacilityResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleItemResponse;

import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleSlideResponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileVehicleDetailResponse {

	private Long id;
	private Long brandId;
	private String brandName;
	private Long modelId;
	private String modelName;
	private String nameKh;
	private String nameEn;
	private String nameZh;
	private String vehicleCode;
	private String plateNumber;
	private String fileName;
	private String fileUrl;
	private Integer quantity;
	private Integer passengers;
	private Double averageRating;
	private Integer totalReviews;
	private Map<Integer, Long> ratingBreakdown;

	private List<VehicleCategoryItemResponse> categories;
	private List<VehicleRentalTypeItemResponse> rentalTypes;
	private List<VehicleSlideResponse> slides;
	private List<VehicleFacilityResponse> facilities;
	private List<VehicleItemResponse> items;
	private List<CustomerReviewResponse> recentReviews;
}
