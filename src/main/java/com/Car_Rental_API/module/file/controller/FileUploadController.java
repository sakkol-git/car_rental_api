package com.Car_Rental_API.module.file.controller;

import com.Car_Rental_API.module.file.service.FileStorageService;
import com.Car_Rental_API.module.file.dto.response.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Car_Rental_API.common.base_dto.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@Tag(name = "03. File Upload", description = "Generic file upload — supports all file types (Images auto-compressed if > 5MB)")
@RequiredArgsConstructor
public class FileUploadController {

	private final FileStorageService storageService;

	// * Upload a single file
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload a file", description = "Stores any file on server (Images auto-compressed) and returns URL/filename")
	public ResponseEntity<BaseResponse<FileUploadResponse>> upload(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(BaseResponse.response(storageService.store(file)));
	}

	// * Delete a file by URL
	@DeleteMapping("/delete")
	@Operation(summary = "Delete a file", description = "Deletes a file from the server by its public URL")
	public ResponseEntity<BaseResponse<String>> delete(@RequestParam("url") String url) {
		if (storageService.delete(url)) {
			return ResponseEntity.ok(BaseResponse.response("File deleted successfully"));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(BaseResponse.error(HttpStatus.NOT_FOUND, "File not found or could not be deleted"));
	}
}
