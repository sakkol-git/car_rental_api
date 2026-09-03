package com.Car_Rental_API.module.master_data.journey_price.service;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.journey_price.model.JourneyPrice;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceFilterRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceResponse;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyVehiclePriceRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyVehiclePriceResponse;
import com.Car_Rental_API.module.master_data.province.service.ProvinceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class JourneyPriceService {

    private final JourneyPriceRepository journeyPriceRepository;
    private final JourneyPriceMapper journeyPriceMapper;
    private final ProvinceService provinceService;

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "journeyPrices", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<JourneyPriceResponse> getAllJourneyPrices(JourneyPriceFilterRequest req) {
        JourneyPriceFilterRequest filter = req != null ? req : new JourneyPriceFilterRequest();
        List<JourneyPrice> journeyPrices = journeyPriceRepository.findAll(filter);
        long total = QueryUtil.shouldCount(filter.getPage(), () -> journeyPriceRepository.countAll(filter));

        return new PageResult<>(toResponses(journeyPrices), total);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "journeyPrice", key = "#id")
    public JourneyPrice getJourneyPriceById(Long id) {
        return journeyPriceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journey Price not found"));
    }

    @CircuitBreaker(name = "defaultService")
    public JourneyPriceResponse getJourneyPriceResponseById(Long id) {
        return toResponses(List.of(getJourneyPriceById(id))).get(0);
    }

    @Transactional
    @CacheEvict(value = {"journeyPrices", "journeyPrice", "dropdown_journeyPrices"}, allEntries = true)
    public void createJourneyPrice(JourneyPriceRequest request, Long userId) {
        validateRequest(request);

        JourneyPrice journeyPrice = journeyPriceMapper.fromCreateRequest(request);
        journeyPrice.setCreated(now());
        journeyPrice.setCreatedBy(userId);

        JourneyPrice saved = journeyPriceRepository.save(journeyPrice);
        journeyPriceRepository.replaceVehiclePrices(saved.getId(), request.getPrices(), userId);
    }

    @Transactional
    @CacheEvict(value = {"journeyPrices", "journeyPrice", "dropdown_journeyPrices"}, allEntries = true)
    public void updateJourneyPrice(Long id, JourneyPriceRequest request, Long userId) {
        validateRequest(request);

        JourneyPrice journeyPrice = getJourneyPriceById(id);
        journeyPriceMapper.updateFromRequest(request, journeyPrice);
        journeyPrice.setModified(now());
        journeyPrice.setModifiedBy(userId);

        journeyPriceRepository.update(journeyPrice);
        journeyPriceRepository.replaceVehiclePrices(id, request.getPrices(), userId);
    }

    @Transactional
    @CacheEvict(value = {"journeyPrices", "journeyPrice", "dropdown_journeyPrices"}, allEntries = true)
    public void deleteJourneyPrice(Long id) {
        getJourneyPriceById(id);
        journeyPriceRepository.deleteById(id);
    }

    // * Attach vehicle price rows after base journey mapping.
    private List<JourneyPriceResponse> toResponses(List<JourneyPrice> journeyPrices) {
        List<JourneyPriceResponse> responses = journeyPriceMapper.toResponses(journeyPrices);
        List<Long> journeyIds = journeyPrices.stream().map(JourneyPrice::getId).toList();

        Map<Long, List<JourneyVehiclePriceResponse>> prices = journeyPriceRepository.findVehiclePricesByJourneyIds(journeyIds);
        responses.forEach(response -> response.setPrices(prices.getOrDefault(response.getId(), List.of())));
        return responses;
    }

    // * Validate referenced provinces and reject duplicate/missing vehicle models.
    private void validateRequest(JourneyPriceRequest request) {
        provinceService.getProvinceById(request.getFromProvinceId());
        provinceService.getProvinceById(request.getToProvinceId());

        Set<Long> modelIds = new HashSet<>(request.getPrices().stream().map(JourneyVehiclePriceRequest::getVehicleModelId).toList());
        if (modelIds.size() != request.getPrices().size()) {
            throw new RuntimeException("Vehicle Model is duplicated");
        }
        if (modelIds.size() != journeyPriceRepository.countActiveVehicleModels(modelIds)) {
            throw new RuntimeException("Vehicle Model not found");
        }
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> getDropdown(BaseFilterRequest req) {
        return journeyPriceRepository.findDropdown(req);
    }

    @CircuitBreaker(name = "defaultService")
    @Cacheable(value = "dropdown_journeyPrices")
    public List<DropdownResponse> getDropdown() {
        return journeyPriceRepository.findDropdown();
    }
}
