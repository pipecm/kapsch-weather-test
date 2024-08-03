package net.kapsch.weather.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForecastCsvFileDto {
    private double latitude;
    private double longitude;
    private String temperatureUnit;
    private String windSpeedUnit;
    private String windDirectionUnit;
}
