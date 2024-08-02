package net.kapsch.weather.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "client.forecast-source")
public class ForecastSourceParameters {
    private String url = "not-set";
    private long timeout = 10;
    private boolean currentWeather = true;
}
