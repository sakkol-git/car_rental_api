package com.Car_Rental_API.module.mobile.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleScheduleResponse {
	private Long vehicleId;
	private String monthYear;
	private List<String> availableDates;
	private List<String> unavailableDates;
	private List<TimeSlotResponse> timeSlots;
}
