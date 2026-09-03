package com.Car_Rental_API.module.master_data.about_us.controller;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsRequest;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsResponse;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/masterdata/about-us")
@RequiredArgsConstructor
@Tag(name = "19. Master Data - About Us", description = "APIs for About Us Management")
public class AboutUsController extends BaseController {

    private final AboutUsService aboutUsService;

    @GetMapping
    @RequiresPermission(module = "About Us", action = "View")
    @Operation(summary = "Get about us content")
    public ResponseEntity<BaseResponse<AboutUsResponse>> getAboutUs() {
        return success(aboutUsService.getAboutUsResponse());
    }

    @PutMapping
    @RequiresPermission(module = "About Us", action = "Edit")
    @Operation(summary = "Update about us content")
    public ResponseEntity<BaseResponse<Void>> updateAboutUs(@Valid @RequestBody AboutUsRequest request) {
        return successVoid(userId -> aboutUsService.updateAboutUs(request, userId));
    }
}
