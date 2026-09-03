package com.Car_Rental_API.module.master_data.about_us.service;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsRequest;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsResponse;
import com.Car_Rental_API.module.master_data.about_us.mapper.AboutUsMapper;
import com.Car_Rental_API.module.master_data.about_us.model.AboutUs;
import com.Car_Rental_API.module.master_data.about_us.repository.AboutUsRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AboutUsService {

    private static final Long ABOUT_US_ID = 1L;

    private final AboutUsRepository aboutUsRepository;
    private final AboutUsMapper aboutUsMapper;

    // * About Us is a singleton row managed with static id = 1.
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAboutUsFallback")
    @Cacheable(value = "aboutUs_detail", key = "'default'")
    public AboutUs getAboutUs() {
        return aboutUsRepository.findById(ABOUT_US_ID)
                .orElseGet(() -> AboutUs.builder().id(ABOUT_US_ID).build());
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getAboutUsResponseFallback")
    public AboutUsResponse getAboutUsResponse() {
        return aboutUsMapper.toResponse(getAboutUs());
    }

    @Transactional
    @CacheEvict(value = {"aboutUs", "aboutUs_detail", "dropdown_aboutUs"}, allEntries = true)
    public void updateAboutUs(AboutUsRequest request, Long userId) {
        AboutUs aboutUs = aboutUsRepository.findById(ABOUT_US_ID).orElse(null);
        if (aboutUs != null) {
            aboutUsMapper.updateFromRequest(request, aboutUs);
            aboutUs.setModified(LocalDateTime.now());
            aboutUs.setModifiedBy(userId);
            aboutUsRepository.update(aboutUs);
        } else {
            aboutUs = aboutUsMapper.fromCreateRequest(request);
            aboutUs.setId(ABOUT_US_ID);
            aboutUs.setCreated(LocalDateTime.now());
            aboutUs.setCreatedBy(userId);
            aboutUsRepository.save(aboutUs);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return aboutUsRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_aboutUs")
    public List<DropdownResponse> getDropdown() {
        return aboutUsRepository.findDropdown();
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private AboutUs getAboutUsFallback(Throwable t) {
        log.error("Circuit breaker open for getAboutUs — cause: {}", t.getMessage(), t);
        throw new GlobalException("About Us service temporarily unavailable", 503);
    }

    private AboutUsResponse getAboutUsResponseFallback(Throwable t) {
        log.error("Circuit breaker open for getAboutUsResponse — cause: {}", t.getMessage(), t);
        throw new GlobalException("About Us service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("About Us service temporarily unavailable", 503);
    }
}
