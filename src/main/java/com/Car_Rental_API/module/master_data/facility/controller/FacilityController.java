package com.Car_Rental_API.module.master_data.facility.controller;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityRequest;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/facilities")
@RequiredArgsConstructor
@Tag(name = "13. Master Data - Facility", description = "APIs for Facility Management")
public class FacilityController extends BaseController {

    private final FacilityService facilityService;

    @GetMapping
    @RequiresPermission(module = "Facility", action = "View")
    @Operation(summary = "Get all facilities with pagination and search")
    public ResponseEntity<BaseResponse<List<FacilityResponse>>> getAllFacilities(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(facilityService.getAllFacilities(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Facility", action = "View")
    @Operation(summary = "Get facility by ID")
    public ResponseEntity<BaseResponse<FacilityResponse>> getFacilityById(@PathVariable Long id) {
        return success(facilityService.getFacilityResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Facility", action = "Add")
    @Operation(summary = "Create new facility")
    public ResponseEntity<BaseResponse<Void>> createFacility(@Valid @RequestBody FacilityRequest request) {
        return successVoid(userId -> facilityService.createFacility(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Facility", action = "Edit")
    @Operation(summary = "Update facility")
    public ResponseEntity<BaseResponse<Void>> updateFacility(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        return successVoid(userId -> facilityService.updateFacility(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Facility", action = "Delete")
    @Operation(summary = "Delete facility")
    public ResponseEntity<BaseResponse<Void>> deleteFacility(@PathVariable Long id) {
        return successVoid(() -> facilityService.deleteFacility(id));
    }
}
