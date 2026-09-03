package com.Car_Rental_API.module.master_data.nationality.controller;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityRequest;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RestController
@RequestMapping("/masterdata/nationalities")
@RequiredArgsConstructor
@Tag(name = "16. Master Data - Nationality", description = "APIs for Nationality Management")
public class NationalityController extends BaseController {

    private final NationalityService nationalityService;

    @GetMapping
    @RequiresPermission(module = "Nationality", action = "View")
    @Operation(summary = "Get all nationalities with pagination and search")
    public ResponseEntity<BaseResponse<List<NationalityResponse>>> getAllNationalities(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(nationalityService.getAllNationalities(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Nationality", action = "View")
    @Operation(summary = "Get nationality by ID")
    public ResponseEntity<BaseResponse<NationalityResponse>> getNationalityById(@PathVariable Long id) {
        return success(nationalityService.getNationalityResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Nationality", action = "Add")
    @Operation(summary = "Create new nationality")
    public ResponseEntity<BaseResponse<Void>> createNationality(@Valid @RequestBody NationalityRequest request) {
        return successVoid(userId -> nationalityService.createNationality(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Nationality", action = "Edit")
    @Operation(summary = "Update nationality")
    public ResponseEntity<BaseResponse<Void>> updateNationality(@PathVariable Long id, @Valid @RequestBody NationalityRequest request) {
        return successVoid(userId -> nationalityService.updateNationality(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Nationality", action = "Delete")
    @Operation(summary = "Delete nationality")
    public ResponseEntity<BaseResponse<Void>> deleteNationality(@PathVariable Long id) {
        return successVoid(() -> nationalityService.deleteNationality(id));
    }
}
