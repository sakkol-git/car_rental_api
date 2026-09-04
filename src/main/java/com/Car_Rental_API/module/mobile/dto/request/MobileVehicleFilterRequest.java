package com.Car_Rental_API.module.mobile.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MobileVehicleFilterRequest extends VehicleFilterRequest {
	private Integer passengers;
	private Double rating;

	// * Hidden Aliases for Mobile API query parameter fallback compatibility
	@JsonIgnore
	@Schema(hidden = true)
	public Long getVehicleCategoryId() {
		return getCategoryId();
	}

	@Schema(hidden = true)
	public void setVehicleCategoryId(Long vehicleCategoryId) {
		setCategoryId(vehicleCategoryId);
	}

	@JsonIgnore
	@Schema(hidden = true)
	public Long getVehicleRentalTypeId() {
		return getRentalTypeId();
	}

	@Schema(hidden = true)
	public void setVehicleRentalTypeId(Long vehicleRentalTypeId) {
		setRentalTypeId(vehicleRentalTypeId);
	}

	@JsonIgnore
	@Schema(hidden = true)
	public Long getVehicleBrandId() {
		return getBrandId();
	}

	@Schema(hidden = true)
	public void setVehicleBrandId(Long vehicleBrandId) {
		setBrandId(vehicleBrandId);
	}

	@JsonIgnore
	@Schema(hidden = true)
	public Long getVehicleModelId() {
		return getModelId();
	}

	@Schema(hidden = true)
	public void setVehicleModelId(Long vehicleModelId) {
		setModelId(vehicleModelId);
	}
}
