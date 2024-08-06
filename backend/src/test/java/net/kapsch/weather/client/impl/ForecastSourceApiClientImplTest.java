package net.kapsch.weather.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import okhttp3.mockwebserver.MockWebServer;
import net.kapsch.weather.ForecastApplicationBaseTest;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.config.ForecastSourceParameters;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class ForecastSourceApiClientImplTest extends ForecastApplicationBaseTest {

    private static ForecastSourceApiClient forecastSourceApiClient;

    public static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String mockWebServerUrl = mockWebServer.url(ROOT_PATH).toString();

        WebClient mockedWebClient = WebClient.builder()
                .baseUrl(mockWebServerUrl)
                .build();

        ForecastSourceParameters parameters = new ForecastSourceParameters();
        parameters.setUrl(mockWebServerUrl);

        forecastSourceApiClient = new ForecastSourceApiClientImpl(mockedWebClient, parameters);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void whenRetrievingFromApiThenSuccess() throws Exception {

        ForecastSourceApiResponse expectedResponse = readFile(API_RESPONSE_PATH, new TypeReference<>() {});

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(expectedResponse))
        );

        Mono<ForecastSourceApiResponse> monoResponse = forecastSourceApiClient.retrieve(LATITUDE_SCL, LONGITUDE_SCL);

        StepVerifier
                .create(monoResponse)
                .expectNextMatches(response -> response.equals(expectedResponse))
                .verifyComplete();
    }
}