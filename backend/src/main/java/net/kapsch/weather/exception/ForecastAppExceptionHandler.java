package net.kapsch.weather.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
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

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleServiceException(Exception exception, WebRequest request) {
        return handleExceptionInternal(
                exception,
                ForecastAppErrorResponse.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .message(exception.getMessage())
                        .build(),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
}
