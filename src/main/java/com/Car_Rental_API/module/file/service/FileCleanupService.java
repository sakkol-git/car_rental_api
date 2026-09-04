package com.Car_Rental_API.module.file.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.Car_Rental_API.common.util.TelegramUtil;
import static com.db_access.jooq.tables.CustomerReviewFiles.CUSTOMER_REVIEW_FILES;
import static com.db_access.jooq.tables.CustomerReviews.CUSTOMER_REVIEWS;
import static com.db_access.jooq.tables.CustomerSupport.CUSTOMER_SUPPORT;
import static com.db_access.jooq.tables.Customers.CUSTOMERS;
import static com.db_access.jooq.tables.Facilities.FACILITIES;
import static com.db_access.jooq.tables.SalesOrderPaymentHistories.SALES_ORDER_PAYMENT_HISTORIES;
import static com.db_access.jooq.tables.SalesOrders.SALES_ORDERS;
import static com.db_access.jooq.tables.UsersApi.USERS_API;
import static com.db_access.jooq.tables.VehicleBrands.VEHICLE_BRANDS;
import static com.db_access.jooq.tables.VehicleCategories.VEHICLE_CATEGORIES;
import static com.db_access.jooq.tables.VehicleRentalTypes.VEHICLE_RENTAL_TYPES;
import static com.db_access.jooq.tables.VehicleSlides.VEHICLE_SLIDES;
import static com.db_access.jooq.tables.Vehicles.VEHICLES;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCleanupService {

	private final DSLContext dsl;
	private final FileStorageService storageService;
	private final TelegramUtil telegram;

	private static final String UPLOADS_PREFIX = "/uploads/";

	// * Scheduled Cleanup Job (Runs every Sunday at 01:00 AM)
	@Scheduled(cron = "0 0 1 * * SUN")
	public void cleanupOrphanFiles() {
		Set<String> referenced = collectReferencedFilenames();
		log.info("[FileCleanup] {} active files referenced in DB.", referenced.size());

		Path dir = Paths.get(storageService.getUploadDir());
		if (!Files.isDirectory(dir)) {
			log.warn("[FileCleanup] Upload dir not found: {}", dir);
			return;
		}

		int deleted = 0, kept = 0, errors = 0;
		try (Stream<Path> files = Files.list(dir)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				String name = file.getFileName().toString();
				if (referenced.contains(name)) {
					kept++;
					continue;
				}
				if (storageService.delete(UPLOADS_PREFIX + name)) {
					deleted++;
					log.info("[FileCleanup] Deleted unused file: {}", name);
				} else {
					errors++;
					log.error("[FileCleanup] Failed to delete file: {}", name);
				}
			}
		} catch (IOException e) {
			log.error("[FileCleanup] Cannot list upload directory: {}", dir, e);
			return;
		}
		log.info("[FileCleanup] Cleanup finished. deleted={}, kept={}, errors={}", deleted, kept, errors);

		// * Notify Telegram with Weekly Summary
		String summary = "Deleted: " + deleted + " | Kept: " + kept + " | Errors: " + errors;
		telegram.sendNotification("📁 Weekly File Cleanup", summary);
	}

	// * Gather Active File References Across All Database Tables
	private Set<String> collectReferencedFilenames() {
		Set<String> refs = new HashSet<>();

		// * 1. Users API (Photo & Signature)
		collect(refs, USERS_API, USERS_API.PHOTO, USERS_API.IS_ACTIVE.ne((byte) 2));
		collect(refs, USERS_API, USERS_API.SIGNATURE, USERS_API.IS_ACTIVE.ne((byte) 2));

		// * 2. Customer Support (File URL & File Name)
		collect(refs, CUSTOMER_SUPPORT, CUSTOMER_SUPPORT.FILE_URL, CUSTOMER_SUPPORT.IS_ACTIVE.eq((byte) 1));
		collect(refs, CUSTOMER_SUPPORT, CUSTOMER_SUPPORT.FILE_NAME, CUSTOMER_SUPPORT.IS_ACTIVE.eq((byte) 1));

		// * 3. Facilities (File URL & File Name)
		collect(refs, FACILITIES, FACILITIES.FILE_URL, FACILITIES.IS_ACTIVE.eq((byte) 1));
		collect(refs, FACILITIES, FACILITIES.FILE_NAME, FACILITIES.IS_ACTIVE.eq((byte) 1));

		// * 4. Vehicle Categories (File URL & File Name)
		collect(refs, VEHICLE_CATEGORIES, VEHICLE_CATEGORIES.FILE_URL, VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1));
		collect(refs, VEHICLE_CATEGORIES, VEHICLE_CATEGORIES.FILE_NAME, VEHICLE_CATEGORIES.IS_ACTIVE.eq((byte) 1));

		// * 5. Vehicle Rental Types (File URL & File Name)
		collect(refs, VEHICLE_RENTAL_TYPES, VEHICLE_RENTAL_TYPES.FILE_URL, VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1));
		collect(refs, VEHICLE_RENTAL_TYPES, VEHICLE_RENTAL_TYPES.FILE_NAME, VEHICLE_RENTAL_TYPES.IS_ACTIVE.eq((byte) 1));

		// * 6. Vehicle Brands (File URL & File Name)
		collect(refs, VEHICLE_BRANDS, VEHICLE_BRANDS.FILE_URL, VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1));
		collect(refs, VEHICLE_BRANDS, VEHICLE_BRANDS.FILE_NAME, VEHICLE_BRANDS.IS_ACTIVE.eq((byte) 1));

		// * 7. Vehicles (File URL & File Name)
		collect(refs, VEHICLES, VEHICLES.FILE_URL, VEHICLES.IS_ACTIVE.eq((byte) 1));
		collect(refs, VEHICLES, VEHICLES.FILE_NAME, VEHICLES.IS_ACTIVE.eq((byte) 1));

		// * 8. Vehicle Slides (Linked to active vehicles)
		dsl.select(VEHICLE_SLIDES.FILE_URL, VEHICLE_SLIDES.FILE_NAME)
				.from(VEHICLE_SLIDES)
				.innerJoin(VEHICLES).on(VEHICLES.ID.eq(VEHICLE_SLIDES.VEHICLE_ID))
				.where(VEHICLES.IS_ACTIVE.eq((byte) 1))
				.fetch().forEach(r -> {
					toFilename(r.value1(), refs);
					toFilename(r.value2(), refs);
				});

		// * 9. Customers (Profile Image / File)
		collect(refs, CUSTOMERS, CUSTOMERS.FILE_URL, CUSTOMERS.IS_ACTIVE.eq((byte) 1));
		collect(refs, CUSTOMERS, CUSTOMERS.FILE_NAME, CUSTOMERS.IS_ACTIVE.eq((byte) 1));

		// * 10. Sales Orders (Payment Receipts)
		collect(refs, SALES_ORDERS, SALES_ORDERS.RECEIPT_FILE_URL, SALES_ORDERS.IS_ACTIVE.eq((byte) 1));
		collect(refs, SALES_ORDERS, SALES_ORDERS.RECEIPT_FILE_NAME, SALES_ORDERS.IS_ACTIVE.eq((byte) 1));

		// * 11. Sales Order Payment Histories (Deposit & Balance Step Receipts)
		dsl.select(SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_FILE_URL, SALES_ORDER_PAYMENT_HISTORIES.RECEIPT_FILE_NAME)
				.from(SALES_ORDER_PAYMENT_HISTORIES)
				.innerJoin(SALES_ORDERS).on(SALES_ORDERS.ID.eq(SALES_ORDER_PAYMENT_HISTORIES.SALES_ORDER_ID))
				.where(SALES_ORDERS.IS_ACTIVE.eq((byte) 1))
				.fetch().forEach(r -> {
					toFilename(r.value1(), refs);
					toFilename(r.value2(), refs);
				});

		// * 12. Customer Review Files (Linked to active reviews)
		dsl.select(CUSTOMER_REVIEW_FILES.FILE_URL, CUSTOMER_REVIEW_FILES.FILE_NAME)
				.from(CUSTOMER_REVIEW_FILES)
				.innerJoin(CUSTOMER_REVIEWS).on(CUSTOMER_REVIEWS.ID.eq(CUSTOMER_REVIEW_FILES.REVIEW_ID))
				.where(CUSTOMER_REVIEWS.IS_ACTIVE.eq((byte) 1))
				.fetch().forEach(r -> {
					toFilename(r.value1(), refs);
					toFilename(r.value2(), refs);
				});

		return refs;
	}

	// * Query Single Field and Extract Filename into Ref Set
	private <R extends Record> void collect(Set<String> refs, Table<R> table, TableField<R, String> field, Condition where) {
		dsl.select(field).from(table).where(where).fetch(field).forEach(val -> toFilename(val, refs));
	}

	// * Convert URL or Filename String into Clean Filename
	private void toFilename(String val, Set<String> refs) {
		if (val == null || val.isBlank()) return;
		if (val.startsWith(UPLOADS_PREFIX)) {
			String name = val.substring(UPLOADS_PREFIX.length()).trim();
			if (!name.isBlank()) refs.add(name);
		} else if (!val.contains("/") && !val.contains("\\")) {
			refs.add(val.trim());
		}
	}
}
