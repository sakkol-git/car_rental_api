package com.Car_Rental_API.module.master_data.privacy_term.service;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermRequest;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermResponse;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.PrivacyTermMapper;
import com.Car_Rental_API.module.master_data.privacy_term.model.PrivacyTerm;
import com.Car_Rental_API.module.master_data.privacy_term.repository.PrivacyTermRepository;
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
public class PrivacyTermService {

    private static final Long PRIVACY_TYPE = 1L;
    private static final Long TERMS_TYPE = 2L;

    private final PrivacyTermRepository privacyTermRepository;
    private final PrivacyTermMapper privacyTermMapper;

    // * Privacy and Terms are singleton rows selected by type: 1 = Privacy, 2 = Terms.
    @CircuitBreaker(name = "defaultService", fallbackMethod = "getPrivacyTermByTypeFallback")
    @Cacheable(value = "privacyTerm", key = "#type")
    public PrivacyTerm getPrivacyTermByType(Long type) {
        validateType(type);
        return privacyTermRepository.findByType(type)
                .orElseGet(() -> PrivacyTerm.builder().type(type).build());
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getPrivacyTermResponseByTypeFallback")
    public PrivacyTermResponse getPrivacyTermResponseByType(Long type) {
        return privacyTermMapper.toResponse(getPrivacyTermByType(type));
    }

    @Transactional
    @CacheEvict(value = {"privacyTerms", "privacyTerm", "dropdown_privacyTerms"}, allEntries = true)
    public void updatePrivacyTerm(PrivacyTermRequest request, Long userId) {
        validateType(request.getType());
        PrivacyTerm privacyTerm = privacyTermRepository.findByType(request.getType()).orElse(null);
        if (privacyTerm != null) {
            privacyTermMapper.updateFromRequest(request, privacyTerm);
            privacyTerm.setModified(LocalDateTime.now());
            privacyTerm.setModifiedBy(userId);
            privacyTermRepository.update(privacyTerm);
        } else {
            privacyTerm = privacyTermMapper.fromCreateRequest(request);
            privacyTerm.setType(request.getType());
            privacyTerm.setCreated(LocalDateTime.now());
            privacyTerm.setCreatedBy(userId);
            privacyTerm.setModified(LocalDateTime.now());
            privacyTerm.setModifiedBy(userId);
            privacyTermRepository.save(privacyTerm);
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return privacyTermRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService", fallbackMethod = "getDropdownFallback")
    @Cacheable(value = "dropdown_privacyTerms")
    public List<DropdownResponse> getDropdown() {
        return privacyTermRepository.findDropdown();
    }

    private void validateType(Long type) {
        if (!PRIVACY_TYPE.equals(type) && !TERMS_TYPE.equals(type)) {
            throw new GlobalException("Type must be 1 (Privacy) or 2 (Terms)", 400);
        }
    }

    // =====================================================================
    // Circuit Breaker Fallback Methods
    // =====================================================================
    private PrivacyTerm getPrivacyTermByTypeFallback(Long type, Throwable t) {
        log.error("Circuit breaker open for getPrivacyTermByType(type={}) — cause: {}", type, t.getMessage(), t);
        throw new GlobalException("Privacy Term service temporarily unavailable", 503);
    }

    private PrivacyTermResponse getPrivacyTermResponseByTypeFallback(Long type, Throwable t) {
        log.error("Circuit breaker open for getPrivacyTermResponseByType(type={}) — cause: {}", type, t.getMessage(), t);
        throw new GlobalException("Privacy Term service temporarily unavailable", 503);
    }

    private List<DropdownResponse> getDropdownFallback(Throwable t) {
        log.error("Circuit breaker open for getDropdown — cause: {}", t.getMessage(), t);
        throw new GlobalException("Privacy Term service temporarily unavailable", 503);
    }
}
