package com.Car_Rental_API.security.authorization.permission.repository;


import java.util.List;
import java.util.Optional;

import com.Car_Rental_API.security.authorization.permission.model.ModuleType;
import com.db_access.jooq.tables.records.ModuleTypesApiRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;


import lombok.RequiredArgsConstructor;

import static com.db_access.jooq.Tables.MODULE_TYPES_API;

@Repository
@RequiredArgsConstructor
public class ModuleTypeRepository {

    private final DSLContext dsl;

    public List<ModuleType> findAll() {
        return dsl.selectFrom(MODULE_TYPES_API).orderBy(MODULE_TYPES_API.ID.desc()).fetchInto(ModuleType.class);
    }

    public Optional<ModuleType> findById(Long id) {
        return dsl.selectFrom(MODULE_TYPES_API).where(MODULE_TYPES_API.ID.eq(id)).fetchOptionalInto(ModuleType.class);
    }

    public Optional<ModuleType> findByName(String name) {
        return dsl.selectFrom(MODULE_TYPES_API).where(MODULE_TYPES_API.NAME.eq(name)).fetchOptionalInto(ModuleType.class);
    }

    // * Update Module Type
    public void update(Long id, String name) {
        dsl.update(MODULE_TYPES_API)
                .set(MODULE_TYPES_API.NAME, name)
                .where(MODULE_TYPES_API.ID.eq(id))
                .execute();
    }

    // * Save Module Type
    public Long save(ModuleType type) {
        ModuleTypesApiRecord record = dsl.newRecord(MODULE_TYPES_API);
        record.from(type);
        record.setId(null);
        record.setStatus((byte)1);

        return dsl.insertInto(MODULE_TYPES_API)
                .set(record)
                .returningResult(MODULE_TYPES_API.ID)
                .fetchOne(MODULE_TYPES_API.ID);
    }

    // * Delete by ID
    public void deleteById(Long id) {
        dsl.deleteFrom(MODULE_TYPES_API).where(MODULE_TYPES_API.ID.eq(id)).execute();
    }

    // * Delete all module types
    public void deleteAll() {
        dsl.deleteFrom(MODULE_TYPES_API).execute();
    }

    // * Truncate module types table
    public void truncate() {
        dsl.truncate(MODULE_TYPES_API).execute();
    }
}