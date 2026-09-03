package com.Car_Rental_API.module.master_data.vehicle_model.controller;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelFilterRequest;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelRequest;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/vehicle-models")
@RequiredArgsConstructor
@Tag(name = "15. Master Data - Vehicle Model", description = "APIs for Vehicle Model Management")
public class VehicleModelController extends BaseController {

    private final VehicleModelService modelService;

    @GetMapping
    @RequiresPermission(module = "Vehicle Model", action = "View")
    @Operation(summary = "Get all vehicle models with pagination and search")
    public ResponseEntity<BaseResponse<List<VehicleModelResponse>>> getAllModels(VehicleModelFilterRequest req) {
        VehicleModelFilterRequest filter = req != null ? req : new VehicleModelFilterRequest();
        return successPage(modelService.getAllModels(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Vehicle Model", action = "View")
    @Operation(summary = "Get vehicle model by ID")
    public ResponseEntity<BaseResponse<VehicleModelResponse>> getModelById(@PathVariable Long id) {
        return success(modelService.getModelResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Vehicle Model", action = "Add")
    @Operation(summary = "Create new vehicle model")
    public ResponseEntity<BaseResponse<Void>> createModel(@Valid @RequestBody VehicleModelRequest request) {
        return successVoid(userId -> modelService.createModel(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Vehicle Model", action = "Edit")
    @Operation(summary = "Update vehicle model")
    public ResponseEntity<BaseResponse<Void>> updateModel(@PathVariable Long id, @Valid @RequestBody VehicleModelRequest request) {
        return successVoid(userId -> modelService.updateModel(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Vehicle Model", action = "Delete")
    @Operation(summary = "Delete vehicle model")
    public ResponseEntity<BaseResponse<Void>> deleteModel(@PathVariable Long id) {
        return successVoid(() -> modelService.deleteModel(id));
    }
}
