package com.Car_Rental_API.module.master_data.vechicle.controller;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleFilterRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleResponse;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleStatusRequest;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/vehicles")
@RequiredArgsConstructor
@Tag(name = "10. Master Data - Vehicle", description = "APIs for Vehicle Management")
public class VehicleController extends BaseController {

    private final VehicleService vehicleService;

    @GetMapping
    @RequiresPermission(module = "Vehicle", action = "View")
    @Operation(summary = "Get all vehicles with pagination and search")
    public ResponseEntity<BaseResponse<List<VehicleResponse>>> getAllVehicles(VehicleFilterRequest req) {
        VehicleFilterRequest filter = req != null ? req : new VehicleFilterRequest();
        return successPage(vehicleService.getAllVehicles(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Vehicle", action = "View")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<BaseResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        return success(vehicleService.getVehicleResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Vehicle", action = "Add")
    @Operation(summary = "Create new vehicle")
    public ResponseEntity<BaseResponse<Void>> createVehicle(@Valid @RequestBody VehicleRequest request) {
        return successVoid(userId -> vehicleService.createVehicle(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Vehicle", action = "Edit")
    @Operation(summary = "Update vehicle")
    public ResponseEntity<BaseResponse<Void>> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return successVoid(userId -> vehicleService.updateVehicle(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    @RequiresPermission(module = "Vehicle", action = "Public To App")
    @Operation(summary = "Update vehicle public status")
    public ResponseEntity<BaseResponse<Void>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusRequest request) {
        return successVoid(userId -> vehicleService.updateStatus(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Vehicle", action = "Delete")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<BaseResponse<Void>> deleteVehicle(@PathVariable Long id) {
        return successVoid(() -> vehicleService.deleteVehicle(id));
    }
}
