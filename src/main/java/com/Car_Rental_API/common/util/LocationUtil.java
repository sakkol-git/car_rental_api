package com.Car_Rental_API.common.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LocationUtil {

	private static final Pattern LAT_LNG_PATTERN = Pattern.compile("(?i)\\s*\\(\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*,\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*\\)\\s*$");
	private static final Set<String> COUNTRIES = Set.of("cambodia", "thailand", "vietnam", "laos", "myanmar", "singapore", "malaysia", "china");

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LocationCoordinates {
		private BigDecimal latitude;
		private BigDecimal longitude;
	}

	// * Extract Latitude and Longitude from Address String (e.g. "... (11.55877, 104.92632)")
	public static LocationCoordinates extractCoordinates(String rawLocation) {
		if (rawLocation == null || rawLocation.isBlank()) {
			return new LocationCoordinates(null, null);
		}
		Matcher matcher = LAT_LNG_PATTERN.matcher(rawLocation);
		if (matcher.find()) {
			try {
				return new LocationCoordinates(new BigDecimal(matcher.group(1)), new BigDecimal(matcher.group(2)));
			} catch (Exception ignored) {}
		}
		return new LocationCoordinates(null, null);
	}

	// * Strip Trailing Coordinates in Parentheses from Address String
	public static String cleanAddressString(String rawLocation) {
		return (rawLocation == null || rawLocation.isBlank())
				? rawLocation
				: LAT_LNG_PATTERN.matcher(rawLocation).replaceAll("").trim();
	}

	// * Extract ONLY the Province Name from Full Address or Google Maps String
	public static String extractProvinceOnly(String rawLocation) {
		if (rawLocation == null || rawLocation.isBlank()) {
			return rawLocation;
		}

		String clean = cleanAddressString(rawLocation);
		String[] parts = clean.split(",");
		List<String> validParts = new ArrayList<>();
		for (String part : parts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) validParts.add(trimmed);
		}

		if (validParts.isEmpty()) return clean;
		if (validParts.size() == 1) return validParts.get(0);

		// Get administrative part before country (or last part)
		String lastPart = validParts.get(validParts.size() - 1);
		String candidate = (COUNTRIES.contains(lastPart.toLowerCase()) && validParts.size() >= 2)
				? validParts.get(validParts.size() - 2)
				: lastPart;

		// Clean city/district prefixes like "Krong ", "Khet ", "Province of "
		String lower = candidate.toLowerCase();
		if (lower.startsWith("krong ")) candidate = candidate.substring(6).trim();
		else if (lower.startsWith("khet ")) candidate = candidate.substring(5).trim();
		else if (lower.startsWith("province of ")) candidate = candidate.substring(12).trim();

		return candidate.isEmpty() ? clean : candidate;
	}
}
