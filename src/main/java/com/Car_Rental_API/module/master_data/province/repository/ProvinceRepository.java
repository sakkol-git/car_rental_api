package com.Car_Rental_API.module.master_data.province.repository;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import com.Car_Rental_API.module.master_data.province.model.Province;
import static com.db_access.jooq.tables.Provinces.PROVINCES;
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
import com.db_access.jooq.tables.records.ProvincesRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProvinceRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> provinceFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(PROVINCES.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = PROVINCES.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String likePattern = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(PROVINCES.NAME.likeIgnoreCase(likePattern));
        }
        return cond;
    }

    public List<Province> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        var query = dsl.select(provinceFields()).from(PROVINCES);

        return addAuditJoins(query, PROVINCES.getName())
                .where(buildCondition(req))
                .orderBy(PROVINCES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Province.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, PROVINCES, buildCondition(req));
    }

    public Optional<Province> findById(Long id) {
        return addAuditJoins(dsl.select(provinceFields()).from(PROVINCES), PROVINCES.getName())
                .where(PROVINCES.ID.eq(id))
                .and(PROVINCES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(Province.class);
    }

    public Province save(Province province) {
        ProvincesRecord record = dsl.newRecord(PROVINCES);
        record.from(province);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(PROVINCES).set(record).execute();

        province.setId(dsl.lastID().longValue());
        return province;
    }

    public void update(Province province) {
        ProvincesRecord record = dsl.newRecord(PROVINCES);
        record.from(province);
        touchModified(record, PROVINCES.MODIFIED, PROVINCES.MODIFIED_BY, province.getModifiedBy());
        record.setId(province.getId());
        record.changed(PROVINCES.ID, false);

        dsl.update(PROVINCES).set(record).where(PROVINCES.ID.eq(province.getId())).execute();
    }

    public int deleteById(Long id) {
        return dsl.update(PROVINCES)
                .set(PROVINCES.IS_ACTIVE, (byte) 0)
                .set(PROVINCES.MODIFIED, LocalDateTime.now())
                .where(PROVINCES.ID.eq(id))
                .execute();
    }


    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, PROVINCES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(PROVINCES.ID, PROVINCES.NAME.as("name"))
                .from(PROVINCES)
                .where(cond)
                .orderBy(PROVINCES.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
