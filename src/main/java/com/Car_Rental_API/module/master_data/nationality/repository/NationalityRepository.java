package com.Car_Rental_API.module.master_data.nationality.repository;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.db_access.jooq.tables.records.NationalitiesRecord;
import com.Car_Rental_API.module.master_data.nationality.model.Nationality;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import static com.Car_Rental_API.common.util.QueryUtil.addAuditJoins;
import static com.Car_Rental_API.common.util.QueryUtil.auditFields;
import static com.Car_Rental_API.common.util.QueryUtil.touchModified;
import static com.db_access.jooq.tables.Nationalities.NATIONALITIES;

@Repository
@RequiredArgsConstructor
public class NationalityRepository {

    private final DSLContext dsl;

    // * Query Fields
    private List<SelectFieldOrAsterisk> nationalityFields() {
        var fields = new ArrayList<SelectFieldOrAsterisk>(List.of(NATIONALITIES.asterisk()));
        fields.addAll(auditFields());
        return fields;
    }

    private Condition buildCondition(BaseFilterRequest req) {
        Condition cond = NATIONALITIES.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String likePattern = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(NATIONALITIES.NAME.likeIgnoreCase(likePattern));
        }
        return cond;
    }

    public List<Nationality> findAll(BaseFilterRequest req) {
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        return addAuditJoins(dsl.select(nationalityFields()).from(NATIONALITIES), NATIONALITIES.getName())
                .where(buildCondition(req))
                .orderBy(NATIONALITIES.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Nationality.class);
    }

    public long countAll(BaseFilterRequest req) {
        return QueryUtil.countAll(dsl, NATIONALITIES, buildCondition(req));
    }

    public Optional<Nationality> findById(Long id) {
        return addAuditJoins(dsl.select(nationalityFields()).from(NATIONALITIES), NATIONALITIES.getName())
                .where(NATIONALITIES.ID.eq(id))
                .and(NATIONALITIES.IS_ACTIVE.eq((byte) 1))
                .fetchOptionalInto(Nationality.class);
    }

    public Nationality save(Nationality nationality) {
        NationalitiesRecord record = dsl.newRecord(NATIONALITIES);
        record.from(nationality);
        record.setId(null);
        record.setCreated(LocalDateTime.now());
        dsl.insertInto(NATIONALITIES).set(record).execute();

        nationality.setId(dsl.lastID().longValue());
        return nationality;
    }

    public void update(Nationality nationality) {
        NationalitiesRecord record = dsl.newRecord(NATIONALITIES);
        record.from(nationality);
        touchModified(record, NATIONALITIES.MODIFIED, NATIONALITIES.MODIFIED_BY, nationality.getModifiedBy());
        record.setId(nationality.getId());
        record.changed(NATIONALITIES.ID, false);

        dsl.update(NATIONALITIES).set(record).where(NATIONALITIES.ID.eq(nationality.getId())).execute();
    }

    public int deleteById(Long id) {
        return dsl.update(NATIONALITIES)
                .set(NATIONALITIES.IS_ACTIVE, (byte) 0)
                .set(NATIONALITIES.MODIFIED, LocalDateTime.now())
                .where(NATIONALITIES.ID.eq(id))
                .execute();
    }


    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = buildCondition(req);
        long total = QueryUtil.countAll(dsl, NATIONALITIES, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(NATIONALITIES.ID, NATIONALITIES.NAME.as("name"))
                .from(NATIONALITIES)
                .where(cond)
                .orderBy(NATIONALITIES.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
