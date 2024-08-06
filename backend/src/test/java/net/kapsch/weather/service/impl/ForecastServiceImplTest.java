package net.kapsch.weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.entity.ForecastRequest;
import net.kapsch.weather.entity.ForecastResponseDataRecord;
import net.kapsch.weather.exception.ForecastAppException;
import net.kapsch.weather.repository.ForecastRequestRepository;
import net.kapsch.weather.repository.ForecastResponseDataRepository;
import org.junit.jupiter.api.BeforeAll;
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

@Log4j2
@ExtendWith(MockitoExtension.class)
class ForecastServiceImplTest {
    private static final double LATITUDE_SCL = -33.5;
    private static final double LONGITUDE_SCL = -70.625;

    private static final UUID REQUEST_ID = UUID.fromString("eeb26662-7469-46db-b691-6719215e68fe");
    private static final UUID RESPONSE_DATA_ID = UUID.fromString("231ba4f7-bddb-4b93-b033-0fcdb6b84f08");

    private static final String API_RESPONSE_PATH = "src/test/resources/responses/api_response_scl.json";
    private static final String REQUEST_FROM_DB_PATH = "src/test/resources/responses/db_forecast_request.json";
    private static final String RESPONSE_DATA_FROM_DB_PATH = "src/test/resources/responses/db_forecast_response_data.json";
    private static final String EXPECTED_CSV_PATH = "src/test/resources/responses/forecast_file_scl.csv";
    private static final String API_EXCEPTION_MSG = "API Error";
    private static final String REPO_EXCEPTION_MSG = "Error retrieving data";
    private static final String NO_EXCEPTION_THROWN = "No exception thrown";

    private static final int CSV_LINES = 4;

    @Mock
    private ForecastRequestRepository forecastRequestRepository;

    @Mock
    private ForecastResponseDataRepository forecastResponseDataRepository;

    @Mock
    private ForecastSourceApiClient forecastSourceApiClient;

    @InjectMocks
    private ForecastServiceImpl forecastService;

    private static ObjectMapper objectMapper;

    @BeforeAll
    protected static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

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

        //Resource expectedCsvResource = new InputStreamResource(new FileInputStream(EXPECTED_CSV_PATH));
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

    private <T> T readFile(String filePath, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(new File(filePath), typeReference);
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