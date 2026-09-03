package com.Car_Rental_API.common.util;

import org.jooq.*;
// import java.lang.Record; <-- REMOVED: This breaks jOOQ!

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

import static com.db_access.jooq.Tables.USERS_API;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

public final class QueryUtil {

    // 1. Added private constructor to prevent accidental instantiation
    private QueryUtil() {}

    public static long countAll(DSLContext dsl, Table<?> table, Condition condition) {
        // 2. Simplified using jOOQ's built-in count methods
        return condition != null ? dsl.fetchCount(table, condition) : dsl.fetchCount(table);
    }

    public static long shouldCount(int page, LongSupplier countSupplier) {
        return page == 1 ? countSupplier.getAsLong() : 0L;
    }

    public static String toCamelCase(String s) {
        // 3. Added isEmpty() check for safety
        if (s == null || s.isEmpty()) return s;

        StringBuilder sb = new StringBuilder(s.length());
        boolean nextCharUpper = false;

        for (char c : s.toCharArray()) {
            if (c == '_') {
                nextCharUpper = true;
            } else {
                sb.append(nextCharUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextCharUpper = false;
            }
        }
        return sb.toString();
    }

    public static List<SelectFieldOrAsterisk> auditFields() {
        // 4. Returned immutable List.of() directly to save memory
        return List.of(
                USERS_API.as("cr").FULL_NAME.as("createdByFullName"),
                USERS_API.as("md").FULL_NAME.as("modifiedByFullName")
        );
    }

    public static List<Field<?>> tableFields(String tableName, String alias, String... extraFields) {
        // 5. Grouped the common fields together cleaner
        List<Field<?>> fields = new ArrayList<>(List.of(
                field(name(alias, "id"), Long.class).as("id"),
                field(name(alias, "name"), String.class).as("name"),
                field(name(alias, "is_active"), Byte.class).as("isActive"),
                field(name(alias, "created"), LocalDateTime.class).as("created"),
                field(name(alias, "created_by"), Long.class).as("createdBy"),
                field(name(alias, "modified"), LocalDateTime.class).as("modified"),
                field(name(alias, "modified_by"), Long.class).as("modifiedBy")
        ));

        if (extraFields != null) {
            for (String f : extraFields) {
                fields.add(field(name(alias, f)).as(toCamelCase(f)));
            }
        }
        return fields;
    }

    public static List<Field<?>> tableFields(Table<?> table, String alias, String... extraFields) {
        return tableFields(table.getName(), alias, extraFields);
    }

    // 6. Explicitly used org.jooq.Record here to avoid Java 14 Record confusion
    public static <R extends org.jooq.Record> SelectOnConditionStep<R> addAuditJoins(SelectJoinStep<R> query, String mainAlias) {
        return query.leftJoin(USERS_API.as("cr"))
                .on(field(name(mainAlias, "created_by"), Long.class).eq( USERS_API.as("cr").ID))
                .leftJoin(USERS_API.as("md"))
                .on(field(name(mainAlias, "modified_by"), Long.class).eq( USERS_API.as("md").ID));
    }

    public static <R extends UpdatableRecord<R>, T> void touchModified(
            R record,
            TableField<R, LocalDateTime> modifiedField,
            TableField<R, T> modifiedByField,
            T modifiedBy
    ) {
        record.set(modifiedField, LocalDateTime.now());
        if (modifiedBy != null) {
            record.set(modifiedByField, modifiedBy);
        }
    }
}