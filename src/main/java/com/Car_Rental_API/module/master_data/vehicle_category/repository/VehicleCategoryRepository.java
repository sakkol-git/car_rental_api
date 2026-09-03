package com.Car_Rental_API.module.master_data.vehicle_category.repository;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import com.Car_Rental_API.module.master_data.vehicle_category.model.VehicleCategory;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.db_access.jooq.tables.records.VehicleCategoriesRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VehicleCategoryRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> categoryFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(VEHICLE_CATEGORIES.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String likePattern = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(
                VEHICLE_CATEGORIES.NAME_KH.likeIgnoreCase(likePattern)
                .or(VEHICLE_CATEGORIES.NAME_EN.likeIgnoreCase(likePattern))
            );
        }
        return cond;
    }

    public List<VehicleCategory> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = dsl.select(categoryFields()).from(VEHICLE_CATEGORIES);

        return addAuditJoins(query, VEHICLE_CATEGORIES.getName())
                .where(buildCondition(req))
                .orderBy(VEHICLE_CATEGORIES.SORT_ORDER.asc(), VEHICLE_CATEGORIES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(VehicleCategory.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, VEHICLE_CATEGORIES, buildCondition(req));
    }

    public Optional<VehicleCategory> findById(Long id) {
        return addAuditJoins(dsl.select(categoryFields()).from(VEHICLE_CATEGORIES), VEHICLE_CATEGORIES.getName())
                .where(VEHICLE_CATEGORIES.ID.eq(id))
                .and(VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(VehicleCategory.class);
    }

    public VehicleCategory save(VehicleCategory category) {
        VehicleCategoriesRecord record = dsl.newRecord(VEHICLE_CATEGORIES);
        record.from(category);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(VEHICLE_CATEGORIES).set(record).execute();

        category.setId(dsl.lastID().longValue());
        return category;
    }

    public void update(VehicleCategory category) {
        VehicleCategoriesRecord record = dsl.newRecord(VEHICLE_CATEGORIES);
        record.from(category);
        touchModified(record, VEHICLE_CATEGORIES.MODIFIED, VEHICLE_CATEGORIES.MODIFIED_BY, category.getModifiedBy());
        record.setId(category.getId());
        record.changed(VEHICLE_CATEGORIES.ID, false);

        dsl.update(VEHICLE_CATEGORIES).set(record).where(VEHICLE_CATEGORIES.ID.eq(category.getId())).execute();
    }

    public void deleteById(Long id) {
        dsl.update(VEHICLE_CATEGORIES)
                .set(VEHICLE_CATEGORIES.IS_ACTIVE, (byte) 0)
                .set(VEHICLE_CATEGORIES.MODIFIED, LocalDateTime.now())
                .where(VEHICLE_CATEGORIES.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, VEHICLE_CATEGORIES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(VEHICLE_CATEGORIES.ID, VEHICLE_CATEGORIES.NAME_EN.as("name"))
                .from(VEHICLE_CATEGORIES)
                .where(cond)
                .orderBy(VEHICLE_CATEGORIES.SORT_ORDER.asc(), VEHICLE_CATEGORIES.NAME_EN.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
