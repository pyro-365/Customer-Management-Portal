package com.customer.service;

import com.customer.dto.Views;
import com.customer.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps service using free & open OpenStreetMap (Nominatim) and OSRM routing.
 * No proprietary Google Maps API keys or billing required.
 */
@Service
@SuppressWarnings("null")
public class MapsService {

    private static final String USER_AGENT = "CustomerManagementWeb/1.0 (contact: support@customermanagement.local)";
    private static final Pattern PINCODE_PATTERN = Pattern.compile("\\b(\\d{6})\\b");

    private final RestClient http;

    public MapsService(RestClient http) {
        this.http = http;
    }

    /**
     * Geocode an address using OpenStreetMap Nominatim with progressive query relaxation.
     */
    public Views.GeoResult geocode(String address) {
        if (address == null || address.isBlank()) {
            throw new ApiException(400, "Please enter an address to verify.");
        }

        List<String> candidates = buildGeocodeCandidates(address.trim());
        for (String query : candidates) {
            Views.GeoResult result = tryNominatimGeocode(query);
            if (result != null) {
                return result;
            }
        }

        throw new ApiException(404, "Address not found on OpenStreetMap. Please check the spelling or add area, city, and PIN code.");
    }

    private List<String> buildGeocodeCandidates(String address) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        
        // 1. Direct query
        candidates.add(address);
        if (!address.toLowerCase().contains("india")) {
            candidates.add(address + ", India");
        }

        // 2. Relax comma-separated segments (e.g. drop door number/flat number like '103/108')
        String[] parts = address.split("[,\\n]+");
        if (parts.length > 1) {
            List<String> trimmedParts = new ArrayList<>();
            for (String part : parts) {
                if (part != null) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        trimmedParts.add(trimmed);
                    }
                }
            }

            if (trimmedParts.size() > 1) {
                String withoutDoor = String.join(", ", trimmedParts.subList(1, trimmedParts.size()));
                candidates.add(withoutDoor);
                if (!withoutDoor.toLowerCase().contains("india")) {
                    candidates.add(withoutDoor + ", India");
                }
            }

            if (trimmedParts.size() >= 3) {
                String subAreaAndCity = String.join(", ", trimmedParts.subList(trimmedParts.size() - 2, trimmedParts.size()));
                candidates.add(subAreaAndCity);
                if (!subAreaAndCity.toLowerCase().contains("india")) {
                    candidates.add(subAreaAndCity + ", India");
                }
            }
        }

        // 3. Match 6-digit postal code if present
        Matcher matcher = PINCODE_PATTERN.matcher(address);
        if (matcher.find()) {
            String pincode = matcher.group(1);
            candidates.add(pincode + ", India");
        }

        return new ArrayList<>(candidates);
    }

    private Views.GeoResult tryNominatimGeocode(String query) {
        try {
            JsonNode res = http.get()
                    .uri("https://nominatim.openstreetmap.org/search?q={q}&format=json&limit=1&addressdetails=1", query)
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(JsonNode.class);

            if (res != null && res.isArray() && !res.isEmpty()) {
                JsonNode first = res.get(0);
                String displayName = first.path("display_name").asText();
                double lat = first.path("lat").asDouble();
                double lon = first.path("lon").asDouble();
                String type = first.path("type").asText(first.path("addresstype").asText("approximate"));
                return new Views.GeoResult(displayName, lat, lon, type);
            }
        } catch (Exception ignored) {
            // Continue to fallback candidates
        }
        return null;
    }

    /**
     * Compute driving route from warehouse/hub to customer destination using OSRM.
     */
    public Views.RouteResult route(double fromLat, double fromLng, double toLat, double toLng) {
        try {
            String uri = String.format(Locale.US,
                    "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                    fromLng, fromLat, toLng, toLat);

            JsonNode res = http.get()
                    .uri(uri)
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(JsonNode.class);

            if (res == null || !"Ok".equalsIgnoreCase(res.path("code").asText())) {
                throw new ApiException(502, "OSRM routing service could not compute a route: "
                        + (res != null ? res.path("message").asText("Unknown error") : "Empty response"));
            }

            JsonNode routes = res.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                throw new ApiException(404, "No driving route found between coordinates.");
            }

            JsonNode primaryRoute = routes.get(0);
            double distMeters = primaryRoute.path("distance").asDouble(0.0);
            double durSec = primaryRoute.path("duration").asDouble(0.0);
            JsonNode geometry = primaryRoute.path("geometry");
            String summary = primaryRoute.path("legs").path(0).path("summary").asText("");

            double distanceKm = Math.round((distMeters / 1000.0) * 10.0) / 10.0;
            double durationMinutes = Math.round(durSec / 60.0);

            return new Views.RouteResult(distanceKm, durationMinutes, geometry, summary);
        } catch (RestClientException e) {
            throw new ApiException(502, "Could not reach OSRM routing service. Check your internet connection.");
        }
    }

    /**
     * Clean fallback SVG representation for coordinate pin.
     */
    public byte[] staticMap(double lat, double lng) {
        String svg = String.format(Locale.US, """
            <svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300">
              <defs>
                <linearGradient id="bgGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                  <stop offset="0%%" stop-color="#1e293b"/>
                  <stop offset="100%%" stop-color="#0f172a"/>
                </linearGradient>
                <pattern id="grid" width="30" height="30" patternUnits="userSpaceOnUse">
                  <path d="M 30 0 L 0 0 0 30" fill="none" stroke="#334155" stroke-width="0.8"/>
                </pattern>
              </defs>
              <rect width="600" height="300" fill="url(#bgGrad)" rx="12"/>
              <rect width="600" height="300" fill="url(#grid)" rx="12"/>
              <circle cx="300" cy="140" r="38" fill="#3b82f6" fill-opacity="0.18"/>
              <circle cx="300" cy="140" r="22" fill="#3b82f6" fill-opacity="0.3"/>
              <g transform="translate(286, 116)">
                <path d="M14 0C6.27 0 0 6.27 0 14c0 10.5 14 26 14 26s14-15.5 14-26c0-7.73-6.27-14-14-14zm0 19c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z" fill="#ef4444"/>
              </g>
              <text x="300" y="200" text-anchor="middle" fill="#f8fafc" font-family="system-ui, sans-serif" font-size="14" font-weight="600">OpenStreetMap Location</text>
              <text x="300" y="226" text-anchor="middle" fill="#94a3b8" font-family="system-ui, sans-serif" font-size="12">Lat: %.5f | Lng: %.5f</text>
            </svg>
            """, lat, lng);
        return svg.getBytes(StandardCharsets.UTF_8);
    }
}
