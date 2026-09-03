package com.Car_Rental_API.module.master_data.vehicle_category.controller;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryRequest;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/vehicle-categories")
@RequiredArgsConstructor
@Tag(name = "08. Master Data - Vehicle Category", description = "APIs for Vehicle Category Management")
public class VehicleCategoryController extends BaseController {

    private final VehicleCategoryService categoryService;

    @GetMapping
    @RequiresPermission(module = "Vehicle Category", action = "View")
    @Operation(summary = "Get all vehicle categories with pagination and search")
    public ResponseEntity<BaseResponse<List<VehicleCategoryResponse>>> getAllCategories(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(categoryService.getAllCategories(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Vehicle Category", action = "View")
    @Operation(summary = "Get vehicle category by ID")
    public ResponseEntity<BaseResponse<VehicleCategoryResponse>> getCategoryById(@PathVariable Long id) {
        return success(categoryService.getCategoryResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Vehicle Category", action = "Add")
    @Operation(summary = "Create new vehicle category")
    public ResponseEntity<BaseResponse<Void>> createCategory(@Valid @RequestBody VehicleCategoryRequest request) {
        return successVoid(userId -> categoryService.createCategory(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Vehicle Category", action = "Edit")
    @Operation(summary = "Update vehicle category")
    public ResponseEntity<BaseResponse<Void>> updateCategory(@PathVariable Long id, @Valid @RequestBody VehicleCategoryRequest request) {
        return successVoid(userId -> categoryService.updateCategory(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Vehicle Category", action = "Delete")
    @Operation(summary = "Delete vehicle category")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(@PathVariable Long id) {
        return successVoid(() -> categoryService.deleteCategory(id));
    }
}
