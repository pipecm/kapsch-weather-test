package net.kapsch.weather.controller.impl;

import net.kapsch.weather.controller.ForecastController;
import net.kapsch.weather.exception.ForecastAppException;
import org.springframework.core.io.Resource;
import lombok.AllArgsConstructor;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.service.ForecastService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@RestController
@RequestMapping("/forecast")
@AllArgsConstructor
public class ForecastControllerImpl implements ForecastController {

    private static final String CONTENT_DISPOSITION_HEADER = "attachment; filename=forecast_data.csv";
    private static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String BAD_REQUEST_EXCEPTION_MSG = "Bad Request: Latitude and longitude are required";

    private final ForecastService forecastService;

    @Override
    @PostMapping
    public Mono<ResponseEntity<Resource>> getForecastCsvFile(@RequestBody ForecastRequestDto forecastRequestDto) {
        if (Stream.of(forecastRequestDto.getLatitude(), forecastRequestDto.getLongitude()).allMatch(ObjectUtils::isEmpty)) {
            return Mono.error(new ForecastAppException(HttpStatus.BAD_REQUEST, BAD_REQUEST_EXCEPTION_MSG));
        }

        return forecastService.generateForecastFile(forecastRequestDto)
                .onErrorResume(Mono::error)
                .map(csv -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,  CONTENT_DISPOSITION_HEADER)
                        .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_CSV)
                        .body(csv));
    }
}
