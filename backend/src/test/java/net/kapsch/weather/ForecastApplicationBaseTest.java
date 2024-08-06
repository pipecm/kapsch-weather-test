package net.kapsch.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import net.kapsch.weather.exception.ForecastAppErrorResponse;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public abstract class ForecastApplicationBaseTest {

    protected static final double LATITUDE_SCL = -33.5;
    protected static final double LONGITUDE_SCL = -70.625;

    protected static final UUID REQUEST_ID = UUID.fromString("eeb26662-7469-46db-b691-6719215e68fe");
    protected static final UUID RESPONSE_DATA_ID = UUID.fromString("231ba4f7-bddb-4b93-b033-0fcdb6b84f08");

    protected static final String FORECAST_ENDPOINT = "/forecast";
    protected static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    protected static final String EXPECTED_CSV_PATH = "src/test/resources/responses/forecast_file_scl.csv";
    protected static final String API_RESPONSE_PATH = "src/test/resources/responses/api_response_scl.json";
    protected static final String REQUEST_FROM_DB_PATH = "src/test/resources/responses/db_forecast_request.json";
    protected static final String RESPONSE_DATA_FROM_DB_PATH = "src/test/resources/responses/db_forecast_response_data.json";
    protected static final String API_EXCEPTION_MSG = "API Error";
    protected static final String REPO_EXCEPTION_MSG = "Error retrieving data";
    protected static final String CONTROLLER_EXCEPTION_MSG = "Some error occurred";
    protected static final String NO_EXCEPTION_THROWN = "No exception thrown";
    protected static final String ROOT_PATH = "/";
    protected static final String BAD_REQUEST_EXCEPTION_MSG = "Bad Request: Latitude and longitude are required";

    protected static final int CSV_LINES = 4;

    protected static ObjectMapper objectMapper;

    @BeforeAll
    protected static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    protected void assertCsvContentsAreEqual(Resource expected, Resource actual) {
        try {
            assertThat(readCsvContents(expected.getInputStream())).containsAll(readCsvContents(actual.getInputStream()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    protected int csvRowCount(Resource resource) {
        try {
            return readCsvContents(resource.getInputStream()).size();
        } catch (Exception e) {
            return 0;
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

    protected <T> T readFile(String filePath, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(new File(filePath), typeReference);
    }

    protected void assertErrorResponse(ForecastAppErrorResponse errorResponse, HttpStatus status, String errorMessage) {
        assertThat(errorResponse.getCode()).isEqualTo(status.value());
        assertThat(errorResponse.getStatus()).isEqualTo(status.name());
        assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
    }
}
