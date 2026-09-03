package com.Car_Rental_API.module.master_data.privacy_term.controller;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermRequest;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/masterdata/privacy-terms")
@RequiredArgsConstructor
@Tag(name = "18. Master Data - Privacy and Terms", description = "APIs for Privacy and Terms Management")
public class PrivacyTermController extends BaseController {

    private final PrivacyTermService privacyTermService;

    @GetMapping
    @RequiresPermission(module = "Privacy and Terms", action = "View")
    @Operation(summary = "Get privacy or terms content by type")
    public ResponseEntity<BaseResponse<PrivacyTermResponse>> getPrivacyTerm(@RequestParam Long type) {
        return success(privacyTermService.getPrivacyTermResponseByType(type));
    }

    @PutMapping
    @RequiresPermission(module = "Privacy and Terms", action = "Edit")
    @Operation(summary = "Update privacy or terms content")
    public ResponseEntity<BaseResponse<Void>> updatePrivacyTerm(@Valid @RequestBody PrivacyTermRequest request) {
        return successVoid(userId -> privacyTermService.updatePrivacyTerm(request, userId));
    }
}
