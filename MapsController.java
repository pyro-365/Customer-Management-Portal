package com.customer.controller;

import com.customer.dto.Requests;
import com.customer.dto.Views;
import com.customer.service.MapsService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/maps")
public class MapsController {

    private final MapsService maps;

    public MapsController(MapsService maps) { this.maps = maps; }

    @PostMapping("/geocode")
    public Views.GeoResult geocode(@Valid @RequestBody Requests.Geocode r) {
        return maps.geocode(r.address().trim());
    }

    @GetMapping("/route")
    public Views.RouteResult route(
            @RequestParam(defaultValue = "13.0827") double fromLat,
            @RequestParam(defaultValue = "80.2707") double fromLng,
            @RequestParam double toLat,
            @RequestParam double toLng) {
        return maps.route(fromLat, fromLng, toLat, toLng);
    }

    @GetMapping(value = "/static", produces = "image/svg+xml")
    public ResponseEntity<byte[]> staticMap(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePrivate())
                .body(maps.staticMap(lat, lng));
    }
}
