package net.kapsch.weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import net.kapsch.weather.ForecastApplicationBaseTest;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.entity.ForecastRequest;
import net.kapsch.weather.entity.ForecastResponseDataRecord;
import net.kapsch.weather.exception.ForecastAppException;
import net.kapsch.weather.repository.ForecastRequestRepository;
import net.kapsch.weather.repository.ForecastResponseDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceImplTest extends ForecastApplicationBaseTest {

    @Mock
    private ForecastRequestRepository forecastRequestRepository;

    @Mock
    private ForecastResponseDataRepository forecastResponseDataRepository;

    @Mock
    private ForecastSourceApiClient forecastSourceApiClient;

    @InjectMocks
    private ForecastServiceImpl forecastService;

    @Test
    void whenGeneratingCsvFileThenCsvFileIsGeneratedOK() throws Exception {
        ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
                                                            .latitude(LATITUDE_SCL)
                                                            .longitude(LONGITUDE_SCL)
                                                            .build();

        ForecastSourceApiResponse apiResponse = readFile(API_RESPONSE_PATH, new TypeReference<>() {});

        ForecastRequest savedForecastRequest = ForecastRequest.builder()
                        .requestId(REQUEST_ID)
                        .latitude(LATITUDE_SCL)
                        .longitude(LONGITUDE_SCL)
                        .build();

        ForecastResponseDataRecord savedRecord = ForecastResponseDataRecord.builder()
                        .recordId(RESPONSE_DATA_ID)
                        .utcTimestamp(LocalDateTime.now(ZoneOffset.UTC))
                        .localTimestamp(LocalDateTime.now())
                        .temperature(12.4)
                        .windSpeed(0.8)
                        .windDirection(117)
                        .build();

        List<ForecastRequest> requestListFromDb = readFile(REQUEST_FROM_DB_PATH, new TypeReference<>() {});
        List<ForecastResponseDataRecord> responseRecordsFromDb = readFile(RESPONSE_DATA_FROM_DB_PATH, new TypeReference<>() {});

        when(forecastSourceApiClient.retrieve(LATITUDE_SCL, LONGITUDE_SCL)).thenReturn(Mono.just(apiResponse));
        when(forecastRequestRepository.save(any(ForecastRequest.class))).thenReturn(savedForecastRequest);
        when(forecastResponseDataRepository.save(any(ForecastResponseDataRecord.class))).thenReturn(savedRecord);
        when(forecastRequestRepository.findByLatitudeAndLongitude(LATITUDE_SCL, LONGITUDE_SCL)).thenReturn(requestListFromDb);

        for (int i = 0; i < requestListFromDb.size(); i++) {
            when(forecastResponseDataRepository.findByForecastRequest(requestListFromDb.get(i)))
                    .thenReturn(Optional.of(responseRecordsFromDb.get(i)));
        }

        Resource expectedCsvResource = new ByteArrayResource(new FileInputStream(EXPECTED_CSV_PATH).readAllBytes());

        StepVerifier
                .create(forecastService.generateForecastFile(forecastRequestDto))
                .assertNext(serviceCsvResource -> assertCsvContentsAreEqual(expectedCsvResource, serviceCsvResource))
                .verifyComplete();

        verify(forecastSourceApiClient).retrieve(LATITUDE_SCL, LONGITUDE_SCL);
        verify(forecastRequestRepository).save(any(ForecastRequest.class));
        verify(forecastResponseDataRepository).save(any(ForecastResponseDataRecord.class));
        verify(forecastRequestRepository).findByLatitudeAndLongitude(LATITUDE_SCL, LONGITUDE_SCL);
        verify(forecastResponseDataRepository, times(CSV_LINES)).findByForecastRequest(any(ForecastRequest.class));
    }

    @Test
    void whenGeneratingCsvFileAndAnAPIErrorOccurredThenError() {
        ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
                .latitude(LATITUDE_SCL)
                .longitude(LONGITUDE_SCL)
                .build();

        when(forecastSourceApiClient.retrieve(LATITUDE_SCL, LONGITUDE_SCL)).thenThrow(new RuntimeException(API_EXCEPTION_MSG));

        try {
            forecastService.generateForecastFile(forecastRequestDto);
            fail(NO_EXCEPTION_THROWN);
        } catch (ForecastAppException e) {
            assertThat(e.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).isEqualTo(API_EXCEPTION_MSG);
        }

        verify(forecastSourceApiClient).retrieve(LATITUDE_SCL, LONGITUDE_SCL);
    }

    @Test
    void whenGeneratingCsvFileAndAnErrorOccurredWhenCreatingTheCsvFileThenError() throws Exception {
        ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
                .latitude(LATITUDE_SCL)
                .longitude(LONGITUDE_SCL)
                .build();

        ForecastSourceApiResponse apiResponse = readFile(API_RESPONSE_PATH, new TypeReference<>() {});

        ForecastRequest savedForecastRequest = ForecastRequest.builder()
                .requestId(REQUEST_ID)
                .latitude(LATITUDE_SCL)
                .longitude(LONGITUDE_SCL)
                .build();

        ForecastResponseDataRecord savedRecord = ForecastResponseDataRecord.builder()
                .recordId(RESPONSE_DATA_ID)
                .utcTimestamp(LocalDateTime.now(ZoneOffset.UTC))
                .localTimestamp(LocalDateTime.now())
                .temperature(12.4)
                .windSpeed(0.8)
                .windDirection(117)
                .build();

        when(forecastSourceApiClient.retrieve(LATITUDE_SCL, LONGITUDE_SCL)).thenReturn(Mono.just(apiResponse));
        when(forecastRequestRepository.save(any(ForecastRequest.class))).thenReturn(savedForecastRequest);
        when(forecastResponseDataRepository.save(any(ForecastResponseDataRecord.class))).thenReturn(savedRecord);
        when(forecastRequestRepository.findByLatitudeAndLongitude(LATITUDE_SCL, LONGITUDE_SCL)).thenThrow(new RuntimeException(REPO_EXCEPTION_MSG));

        StepVerifier
                .create(forecastService.generateForecastFile(forecastRequestDto))
                .expectError()
                .verify();

        verify(forecastSourceApiClient).retrieve(LATITUDE_SCL, LONGITUDE_SCL);
        verify(forecastRequestRepository).save(any(ForecastRequest.class));
        verify(forecastResponseDataRepository).save(any(ForecastResponseDataRecord.class));
        verify(forecastRequestRepository).findByLatitudeAndLongitude(LATITUDE_SCL, LONGITUDE_SCL);
    }
}