package net.kapsch.weather.controller.impl;

import net.kapsch.weather.ForecastApplicationBaseTest;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.exception.ForecastAppErrorResponse;
import net.kapsch.weather.exception.ForecastAppException;
import net.kapsch.weather.service.ForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
class ForecastControllerImplTest extends ForecastApplicationBaseTest {

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setUp(ApplicationContext context) {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @MockBean
    private ForecastService forecastService;

    @Test
    void whenRequestingCsvFileThenCsvFileIsReturnedOK() throws Exception {

        ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
                .latitude(LATITUDE_SCL)
                .longitude(LONGITUDE_SCL)
                .build();

        Resource expectedCsvResource = new ByteArrayResource(new FileInputStream(EXPECTED_CSV_PATH).readAllBytes());

        when(forecastService.generateForecastFile(forecastRequestDto)).thenReturn(Mono.just(expectedCsvResource));

        client
            .post()
            .uri(FORECAST_ENDPOINT)
            .body(BodyInserters.fromValue(forecastRequestDto))
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(CONTENT_TYPE_TEXT_CSV)
            .returnResult(Resource.class)
            .getResponseBody()
            .subscribe(resource -> assertCsvContentsAreEqual(expectedCsvResource, resource));

        verify(forecastService).generateForecastFile(forecastRequestDto);
    }

    @Test
    void whenRequestingCsvFileThenError() {

        ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
                .latitude(LATITUDE_SCL)
                .longitude(LONGITUDE_SCL)
                .build();

        when(forecastService.generateForecastFile(forecastRequestDto))
                .thenThrow(new ForecastAppException(HttpStatus.INTERNAL_SERVER_ERROR, CONTROLLER_EXCEPTION_MSG));

        client
                .post()
                .uri(FORECAST_ENDPOINT)
                .body(BodyInserters.fromValue(forecastRequestDto))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(ForecastAppErrorResponse.class)
                .getResponseBody()
                .subscribe(errorResponse -> assertErrorResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR, CONTROLLER_EXCEPTION_MSG));

        verify(forecastService).generateForecastFile(forecastRequestDto);
    }
}