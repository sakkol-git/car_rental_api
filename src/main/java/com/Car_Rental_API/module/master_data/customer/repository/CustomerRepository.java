package com.Car_Rental_API.module.master_data.customer.repository;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.common.util.QueryUtil;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerFilterRequest;
import com.Car_Rental_API.module.master_data.customer.model.Customer;
import com.db_access.jooq.tables.records.CustomersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.db_access.jooq.Tables.CUSTOMERS;

@Repository
@RequiredArgsConstructor
public class CustomerRepository {

    private final DSLContext dsl;

    private Condition buildCondition(CustomerFilterRequest request) {
        Condition condition = CUSTOMERS.IS_ACTIVE.eq((byte) 1);
        if (request != null) {
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + request.getKeyword().trim() + "%";
                condition = condition.and(
                        CUSTOMERS.FULL_NAME.likeIgnoreCase(likePattern)
                                .or(CUSTOMERS.PHONE_NUMBER.likeIgnoreCase(likePattern))
                                .or(CUSTOMERS.EMAIL.likeIgnoreCase(likePattern)));
            }
            // Apply isVerified filter if provided
            if (request.getIsVerified() != null) {
                condition = condition.and(CUSTOMERS.IS_VERIFIED.eq(request.getIsVerified()));
            }
            // Apply osType filter if provided
            if (request.getOsType() != null) {
                condition = condition.and(CUSTOMERS.OS_TYPE.eq(request.getOsType()));
            }
        }
        return condition;
    }

    public List<Customer> findAll(CustomerFilterRequest request) {
        int limit = request != null ? request.getSize() : 10;
        int offset = request != null ? (request.getPage() - 1) * limit : 0;

        return dsl.selectFrom(CUSTOMERS)
                .where(buildCondition(request))
                .orderBy(CUSTOMERS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Customer.class);
    }

    public long countAll(CustomerFilterRequest request) {
        return dsl.fetchCount(CUSTOMERS, buildCondition(request));
    }

    public Optional<Customer> findById(Long id) {
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(id).and(CUSTOMERS.IS_ACTIVE.eq((byte) 1)))
                .fetchOptionalInto(Customer.class);
    }

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return Optional.empty();
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.PHONE_NUMBER.eq(phone.trim()).and(CUSTOMERS.IS_ACTIVE.eq((byte) 1)))
                .fetchOptionalInto(Customer.class);
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return Optional.empty();
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.EMAIL.eq(email.trim()).and(CUSTOMERS.IS_ACTIVE.eq((byte) 1)))
                .fetchOptionalInto(Customer.class);
    }

    /**
     * Checks if an active customer with the given phone number exists.
     * Uses EXISTS semantics — no full row fetch.
     */
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return false;
        return dsl.fetchExists(
                dsl.selectOne().from(CUSTOMERS)
                        .where(CUSTOMERS.PHONE_NUMBER.eq(phone.trim())
                                .and(CUSTOMERS.IS_ACTIVE.eq((byte) 1))));
    }

    // * Save new Customer Record (insert only, for new customers)
    public Customer save(Customer customer) {
        CustomersRecord record = dsl.newRecord(CUSTOMERS);
        record.from(customer);

        if (customer.getCreated() == null)
            record.setCreated(LocalDateTime.now());
        if (customer.getIsActive() == null)
            record.setIsActive((byte) 1);
        if (customer.getIsVerified() == null)
            record.setIsVerified((byte) 1);

        // For new records (no ID), let the DB assign the identity column
        record.setId(null);
        record.changed(CUSTOMERS.ID, false);

        record.insert();

        // Safely retrieve the generated ID from the record after insert
        Long generatedId = record.getId();
        if (generatedId != null) {
            customer.setId(generatedId);
        } else {
            // Fallback: lastID() — guarded against null
            var lastId = dsl.lastID();
            if (lastId != null) {
                customer.setId(lastId.longValue());
            }
        }
        return customer;
    }

    /**
     * Upsert: insert or update if the ID already exists.
     * Uses ON DUPLICATE KEY UPDATE to atomically handle the race condition.
     */
    public Customer upsert(Customer customer) {
        CustomersRecord record = dsl.newRecord(CUSTOMERS);
        record.from(customer);

        if (customer.getCreated() == null)
            record.setCreated(LocalDateTime.now());
        if (customer.getIsActive() == null)
            record.setIsActive((byte) 1);
        if (customer.getIsVerified() == null)
            record.setIsVerified((byte) 1);

        if (customer.getId() != null) {
            // Attempt upsert: if ID exists, update mutable fields; otherwise insert
            record.setModified(LocalDateTime.now());

            dsl.insertInto(CUSTOMERS)
                    .set(record)
                    .onDuplicateKeyUpdate()
                    .set(CUSTOMERS.FULL_NAME, record.getFullName())
                    .set(CUSTOMERS.PHONE_NUMBER, record.getPhoneNumber())
                    .set(CUSTOMERS.EMAIL, record.getEmail())
                    .set(CUSTOMERS.FILE_NAME, record.getFileName())
                    .set(CUSTOMERS.FILE_URL, record.getFileUrl())
                    .set(CUSTOMERS.IS_VERIFIED, record.getIsVerified())
                    .set(CUSTOMERS.OS_TYPE, record.getOsType())
                    .set(CUSTOMERS.LANGUAGE, record.getLanguage())
                    .set(CUSTOMERS.DEVICE_TOKEN, record.getDeviceToken())
                    .set(CUSTOMERS.MODIFIED, record.getModified())
                    .set(CUSTOMERS.IS_ACTIVE, record.getIsActive())
                    .execute();
            return customer;
        }

        // No ID — pure insert, let DB auto-generate
        return save(customer);
    }

    public void update(Customer customer) {
        // Only update mutable fields — never overwrite id, created, or password
        dsl.update(CUSTOMERS)
                .set(CUSTOMERS.FULL_NAME, customer.getFullName())
                .set(CUSTOMERS.PHONE_NUMBER, customer.getPhoneNumber())
                .set(CUSTOMERS.EMAIL, customer.getEmail())
                .set(CUSTOMERS.FILE_NAME, customer.getFileName())
                .set(CUSTOMERS.FILE_URL, customer.getFileUrl())
                .set(CUSTOMERS.IS_VERIFIED, customer.getIsVerified())
                .set(CUSTOMERS.OS_TYPE, customer.getOsType())
                .set(CUSTOMERS.LANGUAGE, customer.getLanguage())
                .set(CUSTOMERS.DEVICE_TOKEN, customer.getDeviceToken())
                .set(CUSTOMERS.MODIFIED, LocalDateTime.now())
                .where(CUSTOMERS.ID.eq(customer.getId()))
                .execute();
    }

    /**
     * Soft-delete. Returns the number of rows affected (0 = not found).
     */
    public int deleteById(Long id) {
        return dsl.update(CUSTOMERS)
                .set(CUSTOMERS.IS_ACTIVE, (byte) 0)
                .set(CUSTOMERS.MODIFIED, LocalDateTime.now())
                .where(CUSTOMERS.ID.eq(id))
                .execute();
    }

    // * Dropdown Operations
    public PageResult<DropdownResponse> findDropdown(BaseFilterRequest req) {
        Condition cond = CUSTOMERS.IS_ACTIVE.eq((byte) 1);
        if (req != null && req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String like = "%" + req.getKeyword().trim() + "%";
            cond = cond.and(CUSTOMERS.FULL_NAME.likeIgnoreCase(like).or(CUSTOMERS.PHONE_NUMBER.likeIgnoreCase(like)));
        }
        long total = QueryUtil.countAll(dsl, CUSTOMERS, cond);
        int limit = req != null ? req.getSize() : 10;
        int offset = req != null ? (req.getPage() - 1) * limit : 0;

        List<DropdownResponse> data = dsl.select(
                CUSTOMERS.ID.as("id"),
                CUSTOMERS.FULL_NAME.as("name"))
                .from(CUSTOMERS)
                .where(cond)
                .orderBy(CUSTOMERS.FULL_NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(DropdownResponse.class);

        return new PageResult<>(data, total);
    }

    public List<DropdownResponse> findDropdown() {
        return findDropdown(null).data();
    }
}
