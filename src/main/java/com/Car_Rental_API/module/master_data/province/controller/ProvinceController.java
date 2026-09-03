package com.Car_Rental_API.module.master_data.province.controller;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceRequest;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterdata/provinces")
@RequiredArgsConstructor
@Tag(name = "07. Master Data - Province", description = "APIs for Province Management")
public class ProvinceController extends BaseController {

    private final ProvinceService provinceService;

    @GetMapping
    @RequiresPermission(module = "Province", action = "View")
    @Operation(summary = "Get all provinces with pagination and search")
    public ResponseEntity<BaseResponse<List<ProvinceResponse>>> getAllProvinces(BaseFilterRequest req) {
        BaseFilterRequest filter = req != null ? req : new BaseFilterRequest();
        return successPage(provinceService.getAllProvinces(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Province", action = "View")
    @Operation(summary = "Get province by ID")
    public ResponseEntity<BaseResponse<ProvinceResponse>> getProvinceById(@PathVariable Long id) {
        return success(provinceService.getProvinceResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Province", action = "Add")
    @Operation(summary = "Create new province")
    public ResponseEntity<BaseResponse<Void>> createProvince(@Valid @RequestBody ProvinceRequest request) {
        return successVoid(userId -> provinceService.createProvince(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Province", action = "Edit")
    @Operation(summary = "Update province")
    public ResponseEntity<BaseResponse<Void>> updateProvince(@PathVariable Long id, @Valid @RequestBody ProvinceRequest request) {
        return successVoid(userId -> provinceService.updateProvince(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Province", action = "Delete")
    @Operation(summary = "Delete province")
    public ResponseEntity<BaseResponse<Void>> deleteProvince(@PathVariable Long id) {
        return successVoid(() -> provinceService.deleteProvince(id));
    }
}
