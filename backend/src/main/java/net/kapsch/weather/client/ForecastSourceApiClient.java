package net.kapsch.weather.client;

import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import reactor.core.publisher.Mono;

public interface ForecastSourceApiClient {
    Mono<ForecastSourceApiResponse> retrieve(double latitude, double longitude);
}
