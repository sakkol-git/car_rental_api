package com.Car_Rental_API.security.authorization.permission.repository;


import java.util.List;
import java.util.Optional;

import com.db_access.jooq.tables.records.ModulesApiRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.Car_Rental_API.security.authorization.permission.model.Module;


import lombok.RequiredArgsConstructor;

import static com.db_access.jooq.Tables.MODULES_API;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {

    private final DSLContext dsl;

    // * Find All Modules
    public List<Module> findAll() {
        return dsl.selectFrom(MODULES_API).orderBy(MODULES_API.ORDERING).fetchInto(Module.class);
    }

    // * Find by Module Type
    public List<Module> findByModule_type_id(Long module_type_id) {
        return dsl.selectFrom(MODULES_API)
                .where(MODULES_API.MODULE_TYPE_ID.eq(module_type_id))
                .orderBy(MODULES_API.ORDERING)
                .fetchInto(Module.class);
    }

    // * Find by Status
    public List<Module> findByStatus(byte status) {
        return dsl.selectFrom(MODULES_API)
                .where(MODULES_API.STATUS.eq(status))
                .orderBy(MODULES_API.ORDERING)
                .fetchInto(Module.class);
    }

    // * Find by Name (safely limit 1 to prevent TooManyRowsException)
    public Optional<Module> findByName(String name) {
        return dsl.selectFrom(MODULES_API)
                .where(MODULES_API.NAME.likeIgnoreCase(name))
                .limit(1)
                .fetchOptionalInto(Module.class);
    }

    // * Find by Name and Type
    public Optional<Module> findByNameAndType(String name, String type) {
        if (type == null || type.isEmpty()) {
            return findByName(name);
        }
        return dsl.selectFrom(MODULES_API)
                .where(MODULES_API.NAME.likeIgnoreCase(name))
                .and(MODULES_API.TYPE.likeIgnoreCase(type))
                .limit(1)
                .fetchOptionalInto(Module.class);
    }

    // * Find by ID
    public Optional<Module> findById(Long id) {
        return dsl.selectFrom(MODULES_API).where(MODULES_API.ID.eq(id)).fetchOptionalInto(Module.class);
    }

    // * Save Module
    public void save(Module module) {
        ModulesApiRecord record = dsl.newRecord(MODULES_API);
        record.from(module);
        record.setId(null);
        record.setStatus((byte)1);
        dsl.insertInto(MODULES_API).set(record).execute();
    }

    // * Update Module
    public void update(Module module) {
        dsl.update(MODULES_API)
                .set(MODULES_API.NAME, module.getName())
                .set(MODULES_API.TYPE, module.getType())
                .set(MODULES_API.ORDERING, module.getOrdering())
                .where(MODULES_API.ID.eq(module.getId()))
                .execute();
    }

    // * Delete Module by ID
    public void deleteById(Long id) {
        dsl.deleteFrom(MODULES_API).where(MODULES_API.ID.eq(id)).execute();
    }

    // * Delete modules by type
    public void deleteByModule_type_id(Long module_type_id) {
        dsl.deleteFrom(MODULES_API).where(MODULES_API.MODULE_TYPE_ID.eq(module_type_id)).execute();
    }

    // * Delete all modules
    public void deleteAll() {
        dsl.deleteFrom(MODULES_API).execute();
    }

    // * Truncate modules table
    public void truncate() {
        dsl.truncate(MODULES_API).execute();
    }
}
