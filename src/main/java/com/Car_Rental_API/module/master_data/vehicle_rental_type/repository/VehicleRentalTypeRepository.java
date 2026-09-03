package com.Car_Rental_API.module.master_data.vehicle_rental_type.repository;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;

import com.Car_Rental_API.common.base_dto.response.DropdownResponse;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.db_access.jooq.tables.records.VehicleRentalTypesRecord;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.VehicleRentalType;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeFilterRequest;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleRentalTypes.VEHICLE_RENTAL_TYPES;

@Repository
@RequiredArgsConstructor
public class VehicleRentalTypeRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> rentalTypeFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(
                VEHICLE_RENTAL_TYPES.asterisk(),
                VEHICLE_CATEGORIES.NAME_KH.as("categoryNameKh"),
                VEHICLE_CATEGORIES.NAME_EN.as("categoryNameEn"),
                VEHICLE_CATEGORIES.NAME_ZH.as("categoryNameZh")));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(VehicleRentalTypeFilterRequest req) {
        Condition cond = VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1);
        if (req != null) {
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                cond = cond.and(
                        VEHICLE_RENTAL_TYPES.NAME_KH.likeIgnoreCase(likePattern)
                                .or(VEHICLE_RENTAL_TYPES.NAME_EN.likeIgnoreCase(likePattern))
                                .or(VEHICLE_RENTAL_TYPES.NAME_ZH.likeIgnoreCase(likePattern))
                                .or(VEHICLE_CATEGORIES.NAME_KH.likeIgnoreCase(likePattern))
                                .or(VEHICLE_CATEGORIES.NAME_EN.likeIgnoreCase(likePattern))
                                .or(VEHICLE_CATEGORIES.NAME_ZH.likeIgnoreCase(likePattern)));
            }
            if (req.getCategoryId() != null) {
                cond = cond.and(VEHICLE_RENTAL_TYPES.CATEGORY_ID.eq(req.getCategoryId()));
            }
        }
        return cond;
    }

    public List<VehicleRentalType> findAll(VehicleRentalTypeFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = addAuditJoins(
                dsl.select(rentalTypeFields()).from(VEHICLE_RENTAL_TYPES),
                VEHICLE_RENTAL_TYPES.getName())
                .leftJoin(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(VEHICLE_RENTAL_TYPES.CATEGORY_ID));

        return query
                .where(buildCondition(req))
                .orderBy(VEHICLE_RENTAL_TYPES.SORT_ORDER.asc(), VEHICLE_RENTAL_TYPES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(VehicleRentalType.class);
    }

    public long countAll(VehicleRentalTypeFilterRequest req) {
        return dsl.selectCount()
                .from(VEHICLE_RENTAL_TYPES)
                .leftJoin(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(VEHICLE_RENTAL_TYPES.CATEGORY_ID))
                .where(buildCondition(req))
                .fetchOne(0, long.class);
    }

    public Optional<VehicleRentalType> findById(Long id) {
        var query = addAuditJoins(
                dsl.select(rentalTypeFields()).from(VEHICLE_RENTAL_TYPES),
                VEHICLE_RENTAL_TYPES.getName())
                .leftJoin(VEHICLE_CATEGORIES).on(VEHICLE_CATEGORIES.ID.eq(VEHICLE_RENTAL_TYPES.CATEGORY_ID));

        return query
                .where(VEHICLE_RENTAL_TYPES.ID.eq(id))
                .and(VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(VehicleRentalType.class);
    }

    public VehicleRentalType save(VehicleRentalType rentalType) {
        VehicleRentalTypesRecord record = dsl.newRecord(VEHICLE_RENTAL_TYPES);
        record.from(rentalType);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(VEHICLE_RENTAL_TYPES).set(record).execute();

        rentalType.setId(dsl.lastID().longValue());
        return rentalType;
    }

    public void update(VehicleRentalType rentalType) {
        VehicleRentalTypesRecord record = dsl.newRecord(VEHICLE_RENTAL_TYPES);
        record.from(rentalType);
        touchModified(record, VEHICLE_RENTAL_TYPES.MODIFIED, VEHICLE_RENTAL_TYPES.MODIFIED_BY,
                rentalType.getModifiedBy());
        record.setId(rentalType.getId());
        record.changed(VEHICLE_RENTAL_TYPES.ID, false);

        dsl.update(VEHICLE_RENTAL_TYPES).set(record).where(VEHICLE_RENTAL_TYPES.ID.eq(rentalType.getId())).execute();
    }

    public void deleteById(Long id) {
        dsl.update(VEHICLE_RENTAL_TYPES)
                .set(VEHICLE_RENTAL_TYPES.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_RENTAL_TYPES.MODIFIED, LocalDateTime.now())
                .where(VEHICLE_RENTAL_TYPES.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(VEHICLE_RENTAL_TYPES.NAME_EN.likeIgnoreCase(like)
                    .or(VEHICLE_RENTAL_TYPES.NAME_KH.likeIgnoreCase(like)));
        }
        if (req instanceof VehicleRentalTypeFilterRequest rentalReq && rentalReq.getCategoryId() != null) {
            cond = cond.and(VEHICLE_RENTAL_TYPES.CATEGORY_ID.eq(rentalReq.getCategoryId()));
        }
        long total = QueryUtil.countAll(dsl, VEHICLE_RENTAL_TYPES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(VEHICLE_RENTAL_TYPES.ID, VEHICLE_RENTAL_TYPES.NAME_EN.as("name"))
                .from(VEHICLE_RENTAL_TYPES)
                .where(cond)
                .orderBy(VEHICLE_RENTAL_TYPES.SORT_ORDER.asc(), VEHICLE_RENTAL_TYPES.NAME_EN.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
