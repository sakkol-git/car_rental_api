package com.Car_Rental_API.module.master_data.vechicle.repository;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.VehiclesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import java.util.*;

import com.Car_Rental_API.module.mobile.dto.MobileVehicleFilterRequest;
import java.math.BigDecimal;

import static com.db_access.jooq.tables.CustomerReviews.CUSTOMER_REVIEWS;
import static com.db_access.jooq.tables.Facilities.FACILITIES;
import static com.db_access.jooq.tables.VehicleBrands.VEHICLE_BRANDS;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleCategoriesMapping.VEHICLE_CATEGORIES_MAPPING;
import static com.db_access.jooq.tables.VehicleFacilities.VEHICLE_FACILITIES;
import static com.db_access.jooq.tables.VehicleItems.VEHICLE_ITEMS;
import static com.db_access.jooq.tables.VehicleModels.VEHICLE_MODELS;
import static com.db_access.jooq.tables.VehicleRentalTypes.VEHICLE_RENTAL_TYPES;
import static com.db_access.jooq.tables.VehicleRentalTypesMapping.VEHICLE_RENTAL_TYPES_MAPPING;
import static com.db_access.jooq.tables.VehicleSlides.VEHICLE_SLIDES;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;
import static org.jooq.impl.DSL.exists;

@Repository
@RequiredArgsConstructor
public class VehicleRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> vehicleFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(Arrays.asList(VEHICLES.fields()));
        fields.add(VEHICLE_BRANDS.NAME.as("brandName"));
        fields.add(VEHICLE_MODELS.NAME.as("modelName"));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(VehicleFilterRequest req) {
        Condition cond = VEHICLES.IS_ACTIVE.eq((byte) 1);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(
                    VEHICLES.NAME_KH.likeIgnoreCase(likePattern)
                        .or(VEHICLES.NAME_EN.likeIgnoreCase(likePattern))
                        .or(VEHICLES.NAME_ZH.likeIgnoreCase(likePattern))
                        .or(VEHICLES.VEHICLE_CODE.likeIgnoreCase(likePattern))
                        .or(VEHICLES.PLATE_NUMBER.likeIgnoreCase(likePattern))
                        .or(VEHICLE_BRANDS.NAME.likeIgnoreCase(likePattern))
                        .or(VEHICLE_MODELS.NAME.likeIgnoreCase(likePattern))
                );
            }
            if (req.getBrandId() != null) {
                cond = cond.and(VEHICLES.BRAND_ID.eq(req.getBrandId()));
            }
            if (req.getModelId() != null) {
                cond = cond.and(VEHICLES.MODEL_ID.eq(req.getModelId()));
            }
            if (req.getCategoryId() != null) {
                cond = cond.and(exists(dsl.selectOne()
                    .from(VEHICLE_CATEGORIES_MAPPING)
                    .where(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID.eq(VEHICLES.ID))
                    .and(VEHICLE_CATEGORIES_MAPPING.CATEGORY_ID.eq(req.getCategoryId()))));
            }
            if (req.getRentalTypeId() != null) {
                cond = cond.and(exists(dsl.selectOne()
                    .from(VEHICLE_RENTAL_TYPES_MAPPING)
                    .where(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID.eq(VEHICLES.ID))
                    .and(VEHICLE_RENTAL_TYPES_MAPPING.RENTAL_TYPE_ID.eq(req.getRentalTypeId()))));
            }
            if (req.getIsPublic() != null) {
                cond = cond.and(VEHICLES.IS_PUBLIC.eq(req.getIsPublic()));
            }
            if (req instanceof MobileVehicleFilterRequest mobileReq) {
                if (mobileReq.getPassengers() != null) {
                    cond = cond.and(VEHICLES.PASSENGERS.greaterOrEqual(mobileReq.getPassengers()));
                }
                if (mobileReq.getRating() != null) {
                    cond = cond.and(exists(dsl.selectOne()
                        .from(CUSTOMER_REVIEWS)
                        .where(CUSTOMER_REVIEWS.VEHICLE_ID.eq(VEHICLES.ID))
                        .and(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1))
                        .and(CUSTOMER_REVIEWS.IS_DISABLED.eq((byte) 0))
                        .groupBy(CUSTOMER_REVIEWS.VEHICLE_ID)
                        .having(org.jooq.impl.DSL.avg(CUSTOMER_REVIEWS.RATING_STARS).greaterOrEqual(BigDecimal.valueOf(mobileReq.getRating())))));
                }
            }
        }
        return cond;
    }

    public List<Vehicle> findAll(VehicleFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = addAuditJoins(dsl.select(vehicleFields()).from(VEHICLES), VEHICLES.getName())
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLES.BRAND_ID))
                .leftJoin(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(VEHICLES.MODEL_ID));

        return query.where(buildCondition(req))
                .orderBy(VEHICLES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Vehicle.class);
    }

    public long countAll(VehicleFilterRequest req) {
        return dsl.selectCount()
                .from(VEHICLES)
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLES.BRAND_ID))
                .leftJoin(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(VEHICLES.MODEL_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<Vehicle> findById(Long id) {
        var query = addAuditJoins(dsl.select(vehicleFields()).from(VEHICLES), VEHICLES.getName())
                .leftJoin(VEHICLE_BRANDS).on(VEHICLE_BRANDS.ID.eq(VEHICLES.BRAND_ID))
                .leftJoin(VEHICLE_MODELS).on(VEHICLE_MODELS.ID.eq(VEHICLES.MODEL_ID));

        return query.where(VEHICLES.ID.eq(id))
                .and(VEHICLES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(Vehicle.class);
    }

    public Vehicle save(Vehicle vehicle) {
        VehiclesRecord record = dsl.newRecord(VEHICLES);
        record.from(vehicle);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(VEHICLES)
                .set(record)
                .execute();

        vehicle.setId(dsl.lastID().longValue());
        return vehicle;
    }

    public void update(Vehicle vehicle) {
        VehiclesRecord record = dsl.newRecord(VEHICLES);
        record.from(vehicle);
        touchModified(record, VEHICLES.MODIFIED, VEHICLES.MODIFIED_BY, vehicle.getModifiedBy());
        record.setId(vehicle.getId());
        record.changed(VEHICLES.ID, false);

        dsl.update(VEHICLES)
                .set(record)
                .where(VEHICLES.ID.eq(vehicle.getId()))
                .execute();
    }

    public void updateStatus(Long id, Long isPublic, Long modifiedBy) {
        dsl.update(VEHICLES)
                .set(VEHICLES.IS_PUBLIC, isPublic != null ? isPublic.byteValue() : null)
                .set(VEHICLES.MODIFIED, LocalDateTime.now())
                .set(VEHICLES.MODIFIED_BY, modifiedBy)
                .where(VEHICLES.ID.eq(id))
                .and(VEHICLES.IS_ACTIVE.eq((byte) 1))
                .execute();
    }

    public void deleteById(Long id) {
        dsl.update(VEHICLES)
                .set(VEHICLES.IS_ACTIVE, (byte) 0)
                .set(VEHICLES.MODIFIED, LocalDateTime.now())
                .where(VEHICLES.ID.eq(id))
                .execute();

        dsl.update(VEHICLE_ITEMS)
                .set(VEHICLE_ITEMS.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_ITEMS.MODIFIED, LocalDateTime.now())
                .where(VEHICLE_ITEMS.VEHICLE_ID.eq(id))
                .execute();
    }

    public boolean existsActiveBrand(Long brandId) {
        return dsl.fetchExists(VEHICLE_BRANDS, VEHICLE_BRANDS.ID.eq(brandId).and(VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1)));
    }

    public boolean existsActiveModelForBrand(Long modelId, Long brandId) {
        return dsl.fetchExists(VEHICLE_MODELS,
                VEHICLE_MODELS.ID.eq(modelId)
                        .and(VEHICLE_MODELS.BRAND_ID.eq(brandId))
                        .and(VEHICLE_MODELS.IS_ACTIVE.eq((byte) 1)));
    }

    public long countActiveCategories(Set<Long> ids) {
        if (ids.isEmpty()) {
            return 0L;
        }
        return dsl.selectCount()
                .from(VEHICLE_CATEGORIES)
                .where(VEHICLE_CATEGORIES.ID.in(ids))
                .and(VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1))
                .fetchOne(0, long.class);
    }

    public long countActiveRentalTypes(Set<Long> ids) {
        if (ids.isEmpty()) {
            return 0L;
        }
        return dsl.selectCount()
                .from(VEHICLE_RENTAL_TYPES)
                .where(VEHICLE_RENTAL_TYPES.ID.in(ids))
                .and(VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1))
                .fetchOne(0, long.class);
    }

    public long countActiveFacilities(Set<Long> ids) {
        if (ids.isEmpty()) {
            return 0L;
        }
        return dsl.selectCount()
                .from(FACILITIES)
                .where(FACILITIES.ID.in(ids))
                .and(FACILITIES.IS_ACTIVE.eq((byte) 1))
                .fetchOne(0, long.class);
    }

    public void replaceCategoryMappings(Long vehicleId, List<Long> categoryIds) {
        dsl.deleteFrom(VEHICLE_CATEGORIES_MAPPING)
                .where(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID.eq(vehicleId))
                .execute();

        for (Long categoryId : categoryIds) {
            dsl.insertInto(VEHICLE_CATEGORIES_MAPPING)
                    .set(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID, vehicleId)
                    .set(VEHICLE_CATEGORIES_MAPPING.CATEGORY_ID, categoryId)
                    .execute();
        }
    }

    public void replaceRentalTypeMappings(Long vehicleId, List<Long> rentalTypeIds) {
        dsl.deleteFrom(VEHICLE_RENTAL_TYPES_MAPPING)
                .where(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID.eq(vehicleId))
                .execute();

        for (Long rentalTypeId : rentalTypeIds) {
            dsl.insertInto(VEHICLE_RENTAL_TYPES_MAPPING)
                    .set(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID, vehicleId)
                    .set(VEHICLE_RENTAL_TYPES_MAPPING.RENTAL_TYPE_ID, rentalTypeId)
                    .execute();
        }
    }

    public void replaceSlides(Long vehicleId, List<VehicleSlideRequest> slides) {
        dsl.deleteFrom(VEHICLE_SLIDES)
                .where(VEHICLE_SLIDES.VEHICLE_ID.eq(vehicleId))
                .execute();

        for (int i = 0; i < slides.size(); i++) {
            VehicleSlideRequest slide = slides.get(i);
            dsl.insertInto(VEHICLE_SLIDES)
                    .set(VEHICLE_SLIDES.VEHICLE_ID, vehicleId)
                    .set(VEHICLE_SLIDES.FILE_NAME, slide.getFileName())
                    .set(VEHICLE_SLIDES.FILE_URL, slide.getFileUrl())
                    .set(VEHICLE_SLIDES.SORT_ORDER, slide.getSortOrder() != null ? slide.getSortOrder() : i)
                    .execute();
        }
    }

    public void replaceFacilities(Long vehicleId, List<VehicleFacilityRequest> facilities) {
        dsl.deleteFrom(VEHICLE_FACILITIES)
                .where(VEHICLE_FACILITIES.VEHICLE_ID.eq(vehicleId))
                .execute();

        for (VehicleFacilityRequest facility : facilities) {
            dsl.insertInto(VEHICLE_FACILITIES)
                    .set(VEHICLE_FACILITIES.VEHICLE_ID, vehicleId)
                    .set(VEHICLE_FACILITIES.FACILITY_ID, facility.getFacilityId())
                    .set(VEHICLE_FACILITIES.QTY, facility.getQty())
                    .execute();
        }
    }

    public void replaceItems(Long vehicleId, List<VehicleItemRequest> items, Long userId) {
        dsl.update(VEHICLE_ITEMS)
                .set(VEHICLE_ITEMS.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_ITEMS.MODIFIED, LocalDateTime.now())
                .set(VEHICLE_ITEMS.MODIFIED_BY, userId)
                .where(VEHICLE_ITEMS.VEHICLE_ID.eq(vehicleId))
                .execute();

        for (VehicleItemRequest item : items) {
            dsl.insertInto(VEHICLE_ITEMS)
                    .set(VEHICLE_ITEMS.VEHICLE_ID, vehicleId)
                    .set(VEHICLE_ITEMS.CODE, item.getCode())
                    .set(VEHICLE_ITEMS.PLATE_NUMBER, item.getPlateNumber())
                    .set(VEHICLE_ITEMS.STATUS, item.getStatus() != null ? item.getStatus().byteValue() : (byte) 1)
                    .set(VEHICLE_ITEMS.CREATED, LocalDateTime.now())
                    .set(VEHICLE_ITEMS.CREATED_BY, userId)
                    .set(VEHICLE_ITEMS.IS_ACTIVE, (byte) 1)
                    .execute();
        }
    }

    public Map<Long, List<VehicleCategoryItemResponse>> findCategoriesByVehicleIds(List<Long> vehicleIds) {
        Map<Long, List<VehicleCategoryItemResponse>> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.select(
                VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID,
                VEHICLE_CATEGORIES.ID,
                VEHICLE_CATEGORIES.NAME_KH,
                VEHICLE_CATEGORIES.NAME_EN,
                VEHICLE_CATEGORIES.NAME_ZH
            )
            .from(VEHICLE_CATEGORIES_MAPPING)
            .join(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(VEHICLE_CATEGORIES_MAPPING.CATEGORY_ID))
            .where(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID.in(vehicleIds))
            .orderBy(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID.asc(), VEHICLE_CATEGORIES.ID.asc())
            .fetch()
            .forEach(record -> map.computeIfAbsent(record.get(VEHICLE_CATEGORIES_MAPPING.VEHICLE_ID), key -> new ArrayList<>())
                    .add(VehicleCategoryItemResponse.builder()
                            .id(record.get(VEHICLE_CATEGORIES.ID))
                            .nameKh(record.get(VEHICLE_CATEGORIES.NAME_KH))
                            .nameEn(record.get(VEHICLE_CATEGORIES.NAME_EN))
                            .nameZh(record.get(VEHICLE_CATEGORIES.NAME_ZH))
                            .build()));
        return map;
    }

    public Map<Long, List<VehicleRentalTypeItemResponse>> findRentalTypesByVehicleIds(List<Long> vehicleIds) {
        Map<Long, List<VehicleRentalTypeItemResponse>> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.select(
                VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID,
                VEHICLE_RENTAL_TYPES.ID,
                VEHICLE_RENTAL_TYPES.CATEGORY_ID,
                VEHICLE_RENTAL_TYPES.NAME_KH,
                VEHICLE_RENTAL_TYPES.NAME_EN,
                VEHICLE_RENTAL_TYPES.NAME_ZH
            )
            .from(VEHICLE_RENTAL_TYPES_MAPPING)
            .join(VEHICLE_RENTAL_TYPES).on(VEHICLE_RENTAL_TYPES.ID.eq(VEHICLE_RENTAL_TYPES_MAPPING.RENTAL_TYPE_ID))
            .where(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID.in(vehicleIds))
            .orderBy(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID.asc(), VEHICLE_RENTAL_TYPES.ID.asc())
            .fetch()
            .forEach(record -> map.computeIfAbsent(record.get(VEHICLE_RENTAL_TYPES_MAPPING.VEHICLE_ID), key -> new ArrayList<>())
                    .add(VehicleRentalTypeItemResponse.builder()
                            .id(record.get(VEHICLE_RENTAL_TYPES.ID))
                            .categoryId(record.get(VEHICLE_RENTAL_TYPES.CATEGORY_ID))
                            .nameKh(record.get(VEHICLE_RENTAL_TYPES.NAME_KH))
                            .nameEn(record.get(VEHICLE_RENTAL_TYPES.NAME_EN))
                            .nameZh(record.get(VEHICLE_RENTAL_TYPES.NAME_ZH))
                            .build()));
        return map;
    }

    public Map<Long, List<VehicleSlideResponse>> findSlidesByVehicleIds(List<Long> vehicleIds) {
        Map<Long, List<VehicleSlideResponse>> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.selectFrom(VEHICLE_SLIDES)
                .where(VEHICLE_SLIDES.VEHICLE_ID.in(vehicleIds))
                .orderBy(VEHICLE_SLIDES.VEHICLE_ID.asc(), VEHICLE_SLIDES.SORT_ORDER.asc(), VEHICLE_SLIDES.ID.asc())
                .fetch()
                .forEach(record -> map.computeIfAbsent(record.getVehicleId(), key -> new ArrayList<>())
                        .add(VehicleSlideResponse.builder()
                                .id(record.getId())
                                .fileName(record.getFileName())
                                .fileUrl(record.getFileUrl())
                                .sortOrder(record.getSortOrder())
                                .build()));
        return map;
    }

    public Map<Long, List<VehicleFacilityResponse>> findFacilitiesByVehicleIds(List<Long> vehicleIds) {
        Map<Long, List<VehicleFacilityResponse>> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.select(
                VEHICLE_FACILITIES.ID,
                VEHICLE_FACILITIES.VEHICLE_ID,
                VEHICLE_FACILITIES.FACILITY_ID,
                VEHICLE_FACILITIES.QTY,
                FACILITIES.NAME_KH,
                FACILITIES.NAME_EN,
                FACILITIES.NAME_ZH,
                FACILITIES.FILE_NAME,
                FACILITIES.FILE_URL
            )
            .from(VEHICLE_FACILITIES)
            .join(FACILITIES).on(FACILITIES.ID.eq(VEHICLE_FACILITIES.FACILITY_ID))
            .where(VEHICLE_FACILITIES.VEHICLE_ID.in(vehicleIds))
            .orderBy(VEHICLE_FACILITIES.VEHICLE_ID.asc(), VEHICLE_FACILITIES.ID.asc())
            .fetch()
            .forEach(record -> map.computeIfAbsent(record.get(VEHICLE_FACILITIES.VEHICLE_ID), key -> new ArrayList<>())
                    .add(VehicleFacilityResponse.builder()
                            .id(record.get(VEHICLE_FACILITIES.ID))
                            .facilityId(record.get(VEHICLE_FACILITIES.FACILITY_ID))
                            .qty(record.get(VEHICLE_FACILITIES.QTY))
                            .facilityNameKh(record.get(FACILITIES.NAME_KH))
                            .facilityNameEn(record.get(FACILITIES.NAME_EN))
                            .facilityNameZh(record.get(FACILITIES.NAME_ZH))
                            .fileName(record.get(FACILITIES.FILE_NAME))
                            .fileUrl(record.get(FACILITIES.FILE_URL))
                            .build()));
        return map;
    }

    public Map<Long, List<VehicleItemResponse>> findItemsByVehicleIds(List<Long> vehicleIds) {
        Map<Long, List<VehicleItemResponse>> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.selectFrom(VEHICLE_ITEMS)
                .where(VEHICLE_ITEMS.VEHICLE_ID.in(vehicleIds))
                .and(VEHICLE_ITEMS.IS_ACTIVE.eq((byte) 1))
                .orderBy(VEHICLE_ITEMS.VEHICLE_ID.asc(), VEHICLE_ITEMS.ID.asc())
                .fetch()
                .forEach(record -> map.computeIfAbsent(record.getVehicleId(), key -> new ArrayList<>())
                        .add(VehicleItemResponse.builder()
                                .id(record.getId())
                                .code(record.getCode())
                                .plateNumber(record.getPlateNumber())
                                .status(record.getStatus() != null ? record.getStatus().intValue() : null)
                                .created(record.getCreated())
                                .modified(record.getModified())
                                .build()));
        return map;
    }

    public Map<Long, Double> findRatingsByVehicleIds(List<Long> vehicleIds) {
        Map<Long, Double> map = new HashMap<>();
        if (vehicleIds == null || vehicleIds.isEmpty()) return map;

        dsl.select(
                CUSTOMER_REVIEWS.VEHICLE_ID,
                org.jooq.impl.DSL.avg(CUSTOMER_REVIEWS.RATING_STARS)
            )
            .from(CUSTOMER_REVIEWS)
            .where(CUSTOMER_REVIEWS.VEHICLE_ID.in(vehicleIds))
            .and(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1))
            .and(CUSTOMER_REVIEWS.IS_DISABLED.eq((byte) 0))
            .groupBy(CUSTOMER_REVIEWS.VEHICLE_ID)
            .fetch()
            .forEach(record -> {
                Long vId = record.get(CUSTOMER_REVIEWS.VEHICLE_ID);
                BigDecimal avg = record.get(1, BigDecimal.class);
                double roundedAvg = (avg == null) ? 4.5 : Math.round(avg.doubleValue() * 10.0) / 10.0;
                map.put(vId, roundedAvg);
            });
        return map;
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = VEHICLES.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(VEHICLES.NAME_EN.likeIgnoreCase(like).or(VEHICLES.NAME_KH.likeIgnoreCase(like)));
        }
        if (req instanceof VehicleFilterRequest vReq) {
            if (vReq.getBrandId() != null) {
                cond = cond.and(VEHICLES.BRAND_ID.eq(vReq.getBrandId()));
            }
            if (vReq.getModelId() != null) {
                cond = cond.and(VEHICLES.MODEL_ID.eq(vReq.getModelId()));
            }
            if (vReq.getIsPublic() != null) {
                cond = cond.and(VEHICLES.IS_PUBLIC.eq(vReq.getIsPublic()));
            }
        }
        long total = QueryUtil.countAll(dsl, VEHICLES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(VEHICLES.ID, VEHICLES.NAME_EN.as("name"))
                .from(VEHICLES)
                .where(cond)
                .orderBy(VEHICLES.NAME_EN.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }

    // * Find distinct passenger capacities configured across active vehicles
    public List<Integer> findDistinctPassengerCapacities() {
        return dsl.selectDistinct(VEHICLES.PASSENGERS)
                .from(VEHICLES)
                .where(VEHICLES.IS_ACTIVE.eq((byte) 1).and(VEHICLES.PASSENGERS.isNotNull()))
                .orderBy(VEHICLES.PASSENGERS.asc())
                .fetchInto(Integer.class);
    }
}

