package com.Car_Rental_API.module.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
	private String time;
	private Boolean isBusy;
	private Boolean isDisabled;
}
