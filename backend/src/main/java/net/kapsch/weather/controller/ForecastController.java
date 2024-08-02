package net.kapsch.weather.controller;

import org.springframework.core.io.Resource;
import lombok.AllArgsConstructor;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.service.ForecastService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/forecast")
@AllArgsConstructor
public class ForecastController {

    private static final String CONTENT_DISPOSITION_HEADER = "attachment; filename=forecast_data.csv";
    private static final String CONTENT_TYPE_CSV = "text/csv";

    private final ForecastService forecastService;

    @PostMapping
    public Mono<ResponseEntity<Resource>> getForecastCsvFile(@RequestBody ForecastRequestDto forecastRequestDto) {
        return forecastService.generateForecastFile(forecastRequestDto)
                .map(csv -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,  CONTENT_DISPOSITION_HEADER)
                        .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_CSV)
                        .body(csv));
    }
}
