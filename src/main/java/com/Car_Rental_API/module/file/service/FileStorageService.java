package com.Car_Rental_API.module.file.service;

import com.Car_Rental_API.module.file.dto.response.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Car_Rental_API.common.exception.GlobalException;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@Service
public class FileStorageService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	public String getUploadDir() { return uploadDir; }

	private static final long MAX_BYTES = 5 * 1024 * 1024; // 5MB Target
	private static final Set<String> COMPRESSIBLE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

	// * Store: validate → (compress if > 5MB and type is compressible) → save → return response
	public FileUploadResponse store(MultipartFile file) {
		if (file == null || file.isEmpty()) throw new GlobalException("File is required", 400);
		String type = file.getContentType();

		try {
			Files.createDirectories(Paths.get(uploadDir));
			String original = file.getOriginalFilename();
			String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf(".")) : "";
			String filename = UUID.randomUUID() + ext;
			var path = Paths.get(uploadDir, filename);

			if (file.getSize() > MAX_BYTES && isCompressible(type)) {
				log.info("Compressing massive file: {} ({} bytes)", original, file.getSize());
				Files.write(path, aggressiveCompress(file));
			} else {
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			return new FileUploadResponse("/uploads/" + filename, original);
		} catch (IOException e) {
			log.error("Storage error", e);
			throw new GlobalException("Storage error: " + e.getMessage(), 500);
		}
	}

	// * Dispatches to aggressive compression based on type
	private byte[] aggressiveCompress(MultipartFile file) throws IOException {
		String type = file.getContentType();
		try (InputStream is = file.getInputStream()) {
			if (type != null && type.startsWith("image/")) return compressImgAggressive(is);
			return file.getBytes();
		}
	}

	// * Aggressively shrink image (Scale down and low quality)
	private byte[] compressImgAggressive(InputStream is) throws IOException {
		var out = new ByteArrayOutputStream();
		Thumbnails.of(is)
				.size(1920, 1080) 	
				.outputQuality(0.4)
				.outputFormat("jpg") 
				.toOutputStream(out);
		return out.toByteArray();
	}

	// * Check if file type is compressible (Image only)
	private boolean isCompressible(String type) {
		return type != null && COMPRESSIBLE_TYPES.contains(type);
	}

	// * Delete file by URL
	public boolean delete(String url) {
		if (url == null || url.isBlank()) return false;
		try {
			return Files.deleteIfExists(Paths.get(uploadDir, url.replace("/uploads/", "")));
		} catch (IOException e) {
			return false;
		}
	}
}
