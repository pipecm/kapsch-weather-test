package net.kapsch.weather.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForecastAppErrorResponse {
    private int code;
    private String status;
    private String message;
}
