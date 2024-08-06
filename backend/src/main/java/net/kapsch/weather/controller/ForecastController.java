package net.kapsch.weather.controller;

import net.kapsch.weather.dto.ForecastRequestDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ForecastController {
    Mono<ResponseEntity<Resource>> getForecastCsvFile(ForecastRequestDto forecastRequestDto);
}
