package com.Car_Rental_API.module.master_data.journey_price.controller;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


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
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceFilterRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RestController
@RequestMapping("/masterdata/journey-prices")
@RequiredArgsConstructor
@Tag(name = "12. Master Data - Journey Price", description = "APIs for Journey Price Management")
public class JourneyPriceController extends BaseController {

    private final JourneyPriceService journeyPriceService;

    @GetMapping
    @RequiresPermission(module = "Journey Price", action = "View")
    @Operation(summary = "Get all journey prices with pagination and search")
    public ResponseEntity<BaseResponse<List<JourneyPriceResponse>>> getAllJourneyPrices(JourneyPriceFilterRequest req) {
        JourneyPriceFilterRequest filter = req != null ? req : new JourneyPriceFilterRequest();
        return successPage(journeyPriceService.getAllJourneyPrices(filter), filter);
    }

    @GetMapping("/{id}")
    @RequiresPermission(module = "Journey Price", action = "View")
    @Operation(summary = "Get journey price by ID")
    public ResponseEntity<BaseResponse<JourneyPriceResponse>> getJourneyPriceById(@PathVariable Long id) {
        return success(journeyPriceService.getJourneyPriceResponseById(id));
    }

    @PostMapping
    @RequiresPermission(module = "Journey Price", action = "Add")
    @Operation(summary = "Create new journey price")
    public ResponseEntity<BaseResponse<Void>> createJourneyPrice(@Valid @RequestBody JourneyPriceRequest request) {
        return successVoid(userId -> journeyPriceService.createJourneyPrice(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission(module = "Journey Price", action = "Edit")
    @Operation(summary = "Update journey price")
    public ResponseEntity<BaseResponse<Void>> updateJourneyPrice(@PathVariable Long id, @Valid @RequestBody JourneyPriceRequest request) {
        return successVoid(userId -> journeyPriceService.updateJourneyPrice(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "Journey Price", action = "Delete")
    @Operation(summary = "Delete journey price")
    public ResponseEntity<BaseResponse<Void>> deleteJourneyPrice(@PathVariable Long id) {
        return successVoid(() -> journeyPriceService.deleteJourneyPrice(id));
    }
}
