package net.kapsch.weather.client.impl;

import lombok.AllArgsConstructor;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.config.ForecastSourceParameters;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@AllArgsConstructor
public class ForecastSourceApiClientImpl implements ForecastSourceApiClient {

    private static final String FORECAST_ENDPOINT = "/forecast";
    private static final String PARAM_LATITUDE = "latitude";
    private static final String PARAM_LONGITUDE = "longitude";
    private static final String PARAM_CURRENT_WEATHER = "current_weather";

    private final WebClient forecastSourceWebClient;
    private final ForecastSourceParameters forecastSourceParameters;

    @Override
    public Mono<ForecastSourceApiResponse> retrieve(double latitude, double longitude) {
        return forecastSourceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(FORECAST_ENDPOINT)
                        .queryParam(PARAM_LATITUDE, latitude)
                        .queryParam(PARAM_LONGITUDE, longitude)
                        .queryParam(PARAM_CURRENT_WEATHER, forecastSourceParameters.isCurrentWeather())
                        .build())
                .retrieve()
                .bodyToMono(ForecastSourceApiResponse.class)
                .timeout(Duration.ofSeconds(forecastSourceParameters.getTimeout()));
    }
}
