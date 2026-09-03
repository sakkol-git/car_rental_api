package com.Car_Rental_API.module.master_data.sub_location.controller;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationFilterRequest;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationRequest;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationResponse;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationStatusRequest;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RestController
@RequestMapping("/masterdata/sub-locations")
@RequiredArgsConstructor
@Tag(name = "11. Master Data - Sub Location", description = "APIs for Sub Location Management")
public class SubLocationController extends BaseController {

    private final SubLocationService subLocationService;

    @GetMapping
    @RequiresPermission(module = "Sub Location", action = "View")
    @Operation(summary = "Get all sub locations with pagination and search")
    public ResponseEntity<BaseResponse<List<SubLocationResponse>>> getAllSubLocations(SubLocationFilterRequest req) {
        SubLocationFilterRequest filter = req != null ? req : new SubLocationFilterRequest();
        return successPage(subLocationService.getAllSubLocations(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Sub Location", action = "View")
    @Operation(summary = "Get sub location by ID")
    public ResponseEntity<BaseResponse<SubLocationResponse>> getSubLocationById(@PathVariable Long id) {
        return success(subLocationService.getSubLocationResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Sub Location", action = "Add")
    @Operation(summary = "Create new sub location")
    public ResponseEntity<BaseResponse<Void>> createSubLocation(@Valid @RequestBody SubLocationRequest request) {
        return successVoid(userId -> subLocationService.createSubLocation(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Sub Location", action = "Edit")
    @Operation(summary = "Update sub location")
    public ResponseEntity<BaseResponse<Void>> updateSubLocation(@PathVariable Long id, @Valid @RequestBody SubLocationRequest request) {
        return successVoid(userId -> subLocationService.updateSubLocation(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    @RequiresPermission(module = "Sub Location", action = "Public To App")
    @Operation(summary = "Update sub location public status")
    public ResponseEntity<BaseResponse<Void>> updateStatus(@PathVariable Long id, @Valid @RequestBody SubLocationStatusRequest request) {
        return successVoid(userId -> subLocationService.updateStatus(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Sub Location", action = "Delete")
    @Operation(summary = "Delete sub location")
    public ResponseEntity<BaseResponse<Void>> deleteSubLocation(@PathVariable Long id) {
        return successVoid(() -> subLocationService.deleteSubLocation(id));
    }
}
