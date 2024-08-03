package net.kapsch.weather.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForecastAppException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public ForecastAppException(HttpStatus status, String message) {
        super(message);
        this.message = message;
        this.status = status;
    }
}