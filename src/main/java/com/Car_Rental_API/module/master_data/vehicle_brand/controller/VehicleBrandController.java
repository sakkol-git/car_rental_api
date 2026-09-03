package com.Car_Rental_API.module.master_data.vehicle_brand.controller;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandRequest;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/vehicle-brands")
@RequiredArgsConstructor
@Tag(name = "14. Master Data - Vehicle Brand", description = "APIs for Vehicle Brand Management")
public class VehicleBrandController extends BaseController {

    private final VehicleBrandService brandService;

    @GetMapping
    @RequiresPermission(module = "Vehicle Brand", action = "View")
    @Operation(summary = "Get all vehicle brands with pagination and search")
    public ResponseEntity<BaseResponse<List<VehicleBrandResponse>>> getAllBrands(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(brandService.getAllBrands(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Vehicle Brand", action = "View")
    @Operation(summary = "Get vehicle brand by ID")
    public ResponseEntity<BaseResponse<VehicleBrandResponse>> getBrandById(@PathVariable Long id) {
        return success(brandService.getBrandResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Vehicle Brand", action = "Add")
    @Operation(summary = "Create new vehicle brand")
    public ResponseEntity<BaseResponse<Void>> createBrand(@Valid @RequestBody VehicleBrandRequest request) {
        return successVoid(userId -> brandService.createBrand(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Vehicle Brand", action = "Edit")
    @Operation(summary = "Update vehicle brand")
    public ResponseEntity<BaseResponse<Void>> updateBrand(@PathVariable Long id, @Valid @RequestBody VehicleBrandRequest request) {
        return successVoid(userId -> brandService.updateBrand(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Vehicle Brand", action = "Delete")
    @Operation(summary = "Delete vehicle brand")
    public ResponseEntity<BaseResponse<Void>> deleteBrand(@PathVariable Long id) {
        return successVoid(() -> brandService.deleteBrand(id));
    }
}
