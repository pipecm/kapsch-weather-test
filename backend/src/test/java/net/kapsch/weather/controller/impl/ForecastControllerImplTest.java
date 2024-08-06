package net.kapsch.weather.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.service.ForecastService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Log4j2
@WebFluxTest
class ForecastControllerImplTest {
    private static final String FORECAST_ENDPOINT = "/forecast";
    private static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    private static final double LATITUDE_SCL = -33.5;
    private static final double LONGITUDE_SCL = -70.625;
    private static final String EXPECTED_CSV_PATH = "src/test/resources/responses/forecast_file_scl.csv";

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setUp(ApplicationContext context) {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @MockBean
    private ForecastService forecastService;

    private static ObjectMapper objectMapper;

    @BeforeAll
    protected static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

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

    private void assertCsvContentsAreEqual(Resource expected, Resource actual) {
        try {
            assertThat(readCsvContents(expected.getInputStream())).containsAll(readCsvContents(actual.getInputStream()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private List<List<String>> readCsvContents(InputStream inputStream)  {
        List<List<String>> records = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return records;
    }
}