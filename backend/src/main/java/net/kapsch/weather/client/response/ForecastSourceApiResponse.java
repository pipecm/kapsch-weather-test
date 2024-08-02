package net.kapsch.weather.client.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastSourceApiResponse {

    private double latitude;

    private double longitude;

    @JsonProperty("generationtime_ms")
    private double generationTimeMs;

    @JsonProperty("utc_offset_seconds")
    private int utcOffsetSeconds;

    private String timezone;

    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;

    private double elevation;

    @JsonProperty("current_weather_units")
    private CurrentWeatherUnits currentWeatherUnits;

    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CurrentWeatherUnits {

        private String time;

        private String interval;

        private String temperature;

        @JsonProperty("windspeed")
        private String windSpeed;

        @JsonProperty("winddirection")
        private String windDirection;

        @JsonProperty("is_day")
        private String isDay;

        @JsonProperty("weathercode")
        private String weatherCode;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CurrentWeather {

        private LocalDateTime time;

        private int interval;

        private double temperature;

        @JsonProperty("windspeed")
        private double windSpeed;

        @JsonProperty("winddirection")
        private int windDirection;

        @JsonProperty("is_day")
        private int isDay;

        @JsonProperty("weathercode")
        private int weatherCode;
    }
}
