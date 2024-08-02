package net.kapsch.weather.client.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@AllArgsConstructor
public class ForecastSourceConfiguration {

    private final ForecastSourceParameters forecastSourceParameters;

    @Bean
    public WebClient forecastSourceWebClient() {
        return WebClient.builder()
                .baseUrl(forecastSourceParameters.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
