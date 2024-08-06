package net.kapsch.weather.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ForecastAppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ForecastAppException.class)
    protected ResponseEntity<Object> handleServiceException(ForecastAppException exception, WebRequest request) {
        return handleExceptionInternal(
                exception,
                ForecastAppErrorResponse.builder()
                        .code(exception.getStatus().value())
                        .status(exception.getStatus().name())
                        .message(exception.getMessage())
                        .build(),
                new HttpHeaders(),
                exception.getStatus(),
                request
        );
    }
}
