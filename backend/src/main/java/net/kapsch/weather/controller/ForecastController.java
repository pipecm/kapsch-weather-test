package net.kapsch.weather.controller;

import lombok.AllArgsConstructor;
import net.kapsch.weather.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forecast")
@AllArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    public ResponseEntity<?> getAllData(@RequestParam double latitude, @RequestParam double longitude) {

        return ResponseEntity.ok().build();
    }

}
