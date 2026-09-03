package com.Car_Rental_API.module.master_data.vehicle_rental_type.controller;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/vehicle-rental-types")
@RequiredArgsConstructor
@Tag(name = "09. Master Data - Vehicle Rental Type", description = "APIs for Vehicle Rental Type Management")
public class VehicleRentalTypeController extends BaseController {

    private final VehicleRentalTypeService rentalTypeService;

    @GetMapping
    @RequiresPermission(module = "Vehicle Rental Type", action = "View")
    @Operation(summary = "Get all vehicle rental types with pagination and search")
    public ResponseEntity<BaseResponse<List<VehicleRentalTypeResponse>>> getAllRentalTypes(VehicleRentalTypeFilterRequest req) {
        VehicleRentalTypeFilterRequest filter = req != null ? req : new VehicleRentalTypeFilterRequest();
        return successPage(rentalTypeService.getAllRentalTypes(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Vehicle Rental Type", action = "View")
    @Operation(summary = "Get vehicle rental type by ID")
    public ResponseEntity<BaseResponse<VehicleRentalTypeResponse>> getRentalTypeById(@PathVariable Long id) {
        return success(rentalTypeService.getRentalTypeResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Vehicle Rental Type", action = "Add")
    @Operation(summary = "Create new vehicle rental type")
    public ResponseEntity<BaseResponse<Void>> createRentalType(@Valid @RequestBody VehicleRentalTypeRequest request) {
        return successVoid(userId -> rentalTypeService.createRentalType(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Vehicle Rental Type", action = "Edit")
    @Operation(summary = "Update vehicle rental type")
    public ResponseEntity<BaseResponse<Void>> updateRentalType(@PathVariable Long id, @Valid @RequestBody VehicleRentalTypeRequest request) {
        return successVoid(userId -> rentalTypeService.updateRentalType(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Vehicle Rental Type", action = "Delete")
    @Operation(summary = "Delete vehicle rental type")
    public ResponseEntity<BaseResponse<Void>> deleteRentalType(@PathVariable Long id) {
        return successVoid(() -> rentalTypeService.deleteRentalType(id));
    }
}
