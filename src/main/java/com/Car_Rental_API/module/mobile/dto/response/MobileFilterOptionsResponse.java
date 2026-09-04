package com.Car_Rental_API.module.mobile.dto.response;

import java.util.List;

import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileFilterOptionsResponse {
	private List<Integer> passengers;
	private List<Integer> ratings;
	private List<DropdownResponse> categories;
	private List<DropdownResponse> rentalTypes;
	private List<DropdownResponse> brands;
}
