package com.Car_Rental_API.module.master_data.journey_price.repository;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.DropdownWithPriceResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.Provinces;
import com.db_access.jooq.tables.records.JourneysRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.JourneyVehiclePrices.JOURNEY_VEHICLE_PRICES;
import static com.db_access.jooq.tables.Journeys.JOURNEYS;
import static com.db_access.jooq.tables.VehicleModels.VEHICLE_MODELS;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;

@Repository
@RequiredArgsConstructor
public class JourneyPriceRepository {

    private final DSLContext dsl;
    private final Provinces fromProvince = new Provinces("fp");
    private final Provinces toProvince = new Provinces("tp");

    // * Query Fields
    private List<SelectFieldOrAsterisk> journeyFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(Arrays.asList(JOURNEYS.fields()));
        fields.add(fromProvince.NAME.as("fromProvinceName"));
        fields.add(toProvince.NAME.as("toProvinceName"));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(JourneyPriceFilterRequest req) {
        Condition cond = JOURNEYS.IS_ACTIVE.eq((byte) 1);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(
                    fromProvince.NAME.likeIgnoreCase(likePattern)
                        .or(toProvince.NAME.likeIgnoreCase(likePattern))
                        .or(JOURNEYS.DESCRIPTION.likeIgnoreCase(likePattern))
                );
            }
            if (req.getFromProvinceId() != null) {
                cond = cond.and(JOURNEYS.FROM_PROVINCE_ID.eq(req.getFromProvinceId()));
            }
            if (req.getToProvinceId() != null) {
                cond = cond.and(JOURNEYS.TO_PROVINCE_ID.eq(req.getToProvinceId()));
            }
            if (req.getVehicleModelId() != null) {
                cond = cond.and(exists(dsl.selectOne()
                    .from(JOURNEY_VEHICLE_PRICES)
                    .where(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(JOURNEYS.ID))
                    .and(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID.eq(req.getVehicleModelId()))
                    .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1))));
            }
        }
        return cond;
    }

    public List<JourneyPrice> findAll(JourneyPriceFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = addAuditJoins(dsl.select(journeyFields()).from(JOURNEYS), JOURNEYS.getName())
                .leftJoin(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
                .leftJoin(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID));

        return query.where(buildCondition(req))
                .orderBy(JOURNEYS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(JourneyPrice.class);
    }

    public long countAll(JourneyPriceFilterRequest req) {
        return dsl.selectCount()
                .from(JOURNEYS)
                .leftJoin(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
                .leftJoin(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<JourneyPrice> findById(Long id) {
        var query = addAuditJoins(dsl.select(journeyFields()).from(JOURNEYS), JOURNEYS.getName())
                .leftJoin(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
                .leftJoin(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID));

        return query.where(JOURNEYS.ID.eq(id))
                .and(JOURNEYS.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(JourneyPrice.class);
    }

    public JourneyPrice save(JourneyPrice journeyPrice) {
        JourneysRecord record = dsl.newRecord(JOURNEYS);
        record.from(journeyPrice);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(JOURNEYS).set(record).execute();

        journeyPrice.setId(dsl.lastID().longValue());
        return journeyPrice;
    }

    public void update(JourneyPrice journeyPrice) {
        JourneysRecord record = dsl.newRecord(JOURNEYS);
        record.from(journeyPrice);
        touchModified(record, JOURNEYS.MODIFIED, JOURNEYS.MODIFIED_BY, journeyPrice.getModifiedBy());
        record.setId(journeyPrice.getId());
        record.changed(JOURNEYS.ID, false);

        dsl.update(JOURNEYS).set(record).where(JOURNEYS.ID.eq(journeyPrice.getId())).execute();
    }

    public void deleteById(Long id) {
        dsl.update(JOURNEYS)
                .set(JOURNEYS.IS_ACTIVE, (byte) 0)
                .set(JOURNEYS.MODIFIED, LocalDateTime.now())
                .where(JOURNEYS.ID.eq(id))
                .execute();

        dsl.update(JOURNEY_VEHICLE_PRICES)
                .set(JOURNEY_VEHICLE_PRICES.IS_ACTIVE, (byte) 0)
                .set(JOURNEY_VEHICLE_PRICES.MODIFIED, LocalDateTime.now())
                .where(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(id))
                .execute();
    }

    public long countActiveVehicleModels(Set<Long> modelIds) {
        if (modelIds.isEmpty()) return 0L;
        return dsl.selectCount()
                .from(VEHICLE_MODELS)
                .where(VEHICLE_MODELS.ID.in(modelIds))
                .and(VEHICLE_MODELS.IS_ACTIVE.eq((byte) 1))
                .fetchOne(0, long.class);
    }

    public void replaceVehiclePrices(Long journeyId, List<JourneyVehiclePriceRequest> prices, Long userId) {
        dsl.update(JOURNEY_VEHICLE_PRICES)
                .set(JOURNEY_VEHICLE_PRICES.IS_ACTIVE, (byte) 0)
                .set(JOURNEY_VEHICLE_PRICES.MODIFIED, LocalDateTime.now())
                .set(JOURNEY_VEHICLE_PRICES.MODIFIED_BY, userId)
                .where(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(journeyId))
                .execute();

        for (JourneyVehiclePriceRequest price : prices) {
            dsl.insertInto(JOURNEY_VEHICLE_PRICES)
                    .set(JOURNEY_VEHICLE_PRICES.JOURNEY_ID, journeyId)
                    .set(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID, price.getVehicleModelId())
                    .set(JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE, price.getOneWayPrice())
                    .set(JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE, price.getRoundTripPrice())
                    .set(JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE, price.getOneDayTourPrice())
                    .set(JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE, price.getMultiCityPrice())
                    .set(JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE, price.getCityTourPrice())
                    .set(JOURNEY_VEHICLE_PRICES.CREATED, LocalDateTime.now())
                    .set(JOURNEY_VEHICLE_PRICES.CREATED_BY, userId)
                    .set(JOURNEY_VEHICLE_PRICES.IS_ACTIVE, (byte) 1)
                    .execute();
        }
    }

    public Map<Long, List<JourneyVehiclePriceResponse>> findVehiclePricesByJourneyIds(List<Long> journeyIds) {
        Map<Long, List<JourneyVehiclePriceResponse>> map = new HashMap<>();
        if (journeyIds == null || journeyIds.isEmpty()) return map;

        dsl.select(
                JOURNEY_VEHICLE_PRICES.ID,
                JOURNEY_VEHICLE_PRICES.JOURNEY_ID,
                JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID,
                VEHICLE_MODELS.NAME,
                JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE,
                JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE,
                JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE,
                JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE,
                JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE
            )
            .from(JOURNEY_VEHICLE_PRICES)
            .join(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID))
            .where(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.in(journeyIds))
            .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1))
            .orderBy(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.asc(), JOURNEY_VEHICLE_PRICES.ID.asc())
            .fetch()
            .forEach(record -> map.computeIfAbsent(record.get(JOURNEY_VEHICLE_PRICES.JOURNEY_ID), key -> new ArrayList<>())
                    .add(JourneyVehiclePriceResponse.builder()
                            .id(record.get(JOURNEY_VEHICLE_PRICES.ID))
                            .vehicleModelId(record.get(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID))
                            .vehicleModelName(record.get(VEHICLE_MODELS.NAME))
                            .oneWayPrice(record.get(JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE))
                            .roundTripPrice(record.get(JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE))
                            .oneDayTourPrice(record.get(JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE))
                            .multiCityPrice(record.get(JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE))
                            .cityTourPrice(record.get(JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE))
                            .build()));
        return map;
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = JOURNEYS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(JOURNEYS.DESCRIPTION.likeIgnoreCase(like));
        }
        long total = QueryUtil.countAll(dsl, JOURNEYS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(JOURNEYS.ID, JOURNEYS.DESCRIPTION.as("name"))
                .from(JOURNEYS)
                .where(cond)
                .orderBy(JOURNEYS.DESCRIPTION.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }

    // * Journey From-Provinces Dropdown (Provinces that exist in active journeys)
    // * Journey From-Provinces Dropdown (Provinces that exist in active journeys as either Origin or Destination)
    public List<DropdownResponse> findJourneyFromProvinces() {
        var queryFrom = dsl.select(fromProvince.ID.as("id"), fromProvince.NAME.as("name"))
                .from(JOURNEYS)
                .join(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
                .where(JOURNEYS.IS_ACTIVE.eq((byte) 1)).and(fromProvince.IS_ACTIVE.eq((byte) 1));

        var queryTo = dsl.select(toProvince.ID.as("id"), toProvince.NAME.as("name"))
                .from(JOURNEYS)
                .join(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID))
                .where(JOURNEYS.IS_ACTIVE.eq((byte) 1)).and(toProvince.IS_ACTIVE.eq((byte) 1));

        return queryFrom.union(queryTo)
                .orderBy(field("name").asc())
                .fetchInto(DropdownResponse.class);
    }

    // * Journey To-Provinces Dropdown (Provinces that exist in active journeys, filtered by fromProvinceId)
    public List<DropdownResponse> findJourneyToProvinces(Long fromProvinceId) {
        if (fromProvinceId == null) {
            return findJourneyFromProvinces();
        }
        var query1 = dsl.select(toProvince.ID.as("id"), toProvince.NAME.as("name"))
                .from(JOURNEYS)
                .join(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID))
                .where(JOURNEYS.IS_ACTIVE.eq((byte) 1)).and(toProvince.IS_ACTIVE.eq((byte) 1)).and(JOURNEYS.FROM_PROVINCE_ID.eq(fromProvinceId));

        var query2 = dsl.select(fromProvince.ID.as("id"), fromProvince.NAME.as("name"))
                .from(JOURNEYS)
                .join(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
                .where(JOURNEYS.IS_ACTIVE.eq((byte) 1)).and(fromProvince.IS_ACTIVE.eq((byte) 1)).and(JOURNEYS.TO_PROVINCE_ID.eq(fromProvinceId));

        return query1.union(query2)
                .orderBy(field("name").asc())
                .fetchInto(DropdownResponse.class);
    }

    // * Find full journey price record (all price types) for a given route + vehicle model (bidirectional matching)
    public JourneyVehiclePriceResponse findJourneyPriceRecord(Long fromProvinceId, Long toProvinceId, Long vehicleModelId) {
        if (fromProvinceId == null || toProvinceId == null) return null;

        Condition routeCond = (JOURNEYS.FROM_PROVINCE_ID.eq(fromProvinceId).and(JOURNEYS.TO_PROVINCE_ID.eq(toProvinceId)))
                .or(JOURNEYS.FROM_PROVINCE_ID.eq(toProvinceId).and(JOURNEYS.TO_PROVINCE_ID.eq(fromProvinceId)));

        Condition cond = routeCond
                .and(JOURNEYS.IS_ACTIVE.eq((byte) 1))
                .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1));

        if (vehicleModelId != null) cond = cond.and(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID.eq(vehicleModelId));

        var rec = dsl.select(
                JOURNEY_VEHICLE_PRICES.ID,
                JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID,
                VEHICLE_MODELS.NAME,
                JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE,
                JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE,
                JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE,
                JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE,
                JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE
            )
            .from(JOURNEYS)
            .join(JOURNEY_VEHICLE_PRICES).on(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(JOURNEYS.ID))
            .join(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID))
            .where(cond)
            .limit(1)
            .fetchOne();

        if (rec == null) return null;

        return JourneyVehiclePriceResponse.builder()
                .id(rec.get(JOURNEY_VEHICLE_PRICES.ID))
                .vehicleModelId(rec.get(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID))
                .vehicleModelName(rec.get(VEHICLE_MODELS.NAME))
                .oneWayPrice(rec.get(JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE))
                .roundTripPrice(rec.get(JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE))
                .oneDayTourPrice(rec.get(JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE))
                .multiCityPrice(rec.get(JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE))
                .cityTourPrice(rec.get(JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE))
                .build();
    }

    // * Find destination provinces with price for given route (bidirectional A->B or B->A), vehicle model, and journeyType
    public List<DropdownWithPriceResponse> findJourneyToProvincesWithPrice(
            Long fromProvinceId, Long vehicleModelId, Byte journeyType) {

        int type = (journeyType != null) ? journeyType.intValue() : 1;
        Field<BigDecimal> priceField;
        switch (type) {
            case 2 -> priceField = JOURNEY_VEHICLE_PRICES.ONE_DAY_TOUR_PRICE;
            case 3 -> priceField = JOURNEY_VEHICLE_PRICES.ROUND_TRIP_PRICE;
            case 4 -> priceField = JOURNEY_VEHICLE_PRICES.MULTI_CITY_PRICE;
            case 5 -> priceField = JOURNEY_VEHICLE_PRICES.CITY_TOUR_PRICE;
            default -> priceField = JOURNEY_VEHICLE_PRICES.ONE_WAY_PRICE;
        }

        if (fromProvinceId == null) {
            Condition cond = JOURNEYS.IS_ACTIVE.eq((byte) 1).and(toProvince.IS_ACTIVE.eq((byte) 1))
                    .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1));
            if (vehicleModelId != null) cond = cond.and(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID.eq(vehicleModelId));

            return dsl.selectDistinct(toProvince.ID, toProvince.NAME, priceField)
                .from(JOURNEYS)
                .join(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID))
                .join(JOURNEY_VEHICLE_PRICES).on(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(JOURNEYS.ID))
                .where(cond)
                .orderBy(toProvince.NAME.asc())
                .fetch()
                .stream()
                .map(r -> DropdownWithPriceResponse.builder()
                        .id(r.get(toProvince.ID))
                        .name(r.get(toProvince.NAME))
                        .price(r.get(priceField))
                        .build())
                .toList();
        }

        Condition cond1 = JOURNEYS.IS_ACTIVE.eq((byte) 1).and(toProvince.IS_ACTIVE.eq((byte) 1))
                .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1))
                .and(JOURNEYS.FROM_PROVINCE_ID.eq(fromProvinceId));
        if (vehicleModelId != null) cond1 = cond1.and(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID.eq(vehicleModelId));

        var q1 = dsl.select(toProvince.ID.as("dest_id"), toProvince.NAME.as("dest_name"), priceField.as("dest_price"))
            .from(JOURNEYS)
            .join(toProvince).on(toProvince.ID.eq(JOURNEYS.TO_PROVINCE_ID))
            .join(JOURNEY_VEHICLE_PRICES).on(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(JOURNEYS.ID))
            .where(cond1);

        Condition cond2 = JOURNEYS.IS_ACTIVE.eq((byte) 1).and(fromProvince.IS_ACTIVE.eq((byte) 1))
                .and(JOURNEY_VEHICLE_PRICES.IS_ACTIVE.eq((byte) 1))
                .and(JOURNEYS.TO_PROVINCE_ID.eq(fromProvinceId));
        if (vehicleModelId != null) cond2 = cond2.and(JOURNEY_VEHICLE_PRICES.VEHICLE_MODEL_ID.eq(vehicleModelId));

        var q2 = dsl.select(fromProvince.ID.as("dest_id"), fromProvince.NAME.as("dest_name"), priceField.as("dest_price"))
            .from(JOURNEYS)
            .join(fromProvince).on(fromProvince.ID.eq(JOURNEYS.FROM_PROVINCE_ID))
            .join(JOURNEY_VEHICLE_PRICES).on(JOURNEY_VEHICLE_PRICES.JOURNEY_ID.eq(JOURNEYS.ID))
            .where(cond2);

        Field<Long> destIdField = field("dest_id", Long.class);
        Field<String> destNameField = field("dest_name", String.class);
        Field<BigDecimal> destPriceField = field("dest_price", BigDecimal.class);

        return q1.union(q2)
            .orderBy(destNameField.asc())
            .fetch()
            .stream()
            .map(r -> DropdownWithPriceResponse.builder()
                    .id(r.get(destIdField))
                    .name(r.get(destNameField))
                    .price(r.get(destPriceField))
                    .build())
            .toList();
    }
}
