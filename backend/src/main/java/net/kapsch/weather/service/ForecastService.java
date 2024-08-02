package net.kapsch.weather.service;

import org.springframework.core.io.Resource;
import net.kapsch.weather.dto.ForecastRequestDto;
import reactor.core.publisher.Mono;

public interface ForecastService {
    Mono<Resource> generateForecastFile(ForecastRequestDto forecastRequestDto);
}
