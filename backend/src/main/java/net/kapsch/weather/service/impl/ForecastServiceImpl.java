package net.kapsch.weather.service.impl;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.entity.ForecastRequest;
import net.kapsch.weather.entity.ForecastResponseDataRecord;
import net.kapsch.weather.repository.ForecastRequestRepository;
import net.kapsch.weather.repository.ForecastResponseDataRepository;
import net.kapsch.weather.service.ForecastService;
import net.kapsch.weather.util.ByteArrayInOutStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private static final String[] CSV_COLUMNS = {"Local timestamp", "UTC timestamp", "Latitude", "Longitude", "Temperature", "Wind Speed", "Wind Direction"};

    private final ForecastRequestRepository forecastRequestRepository;
    private final ForecastResponseDataRepository forecastResponseDataRepository;
    private final ForecastSourceApiClient forecastSourceApiClient;

    @Override
    public Mono<Resource> generateForecastFile(ForecastRequestDto forecastRequestDto) {
        return forecastSourceApiClient
                .retrieve(forecastRequestDto.getLatitude(), forecastRequestDto.getLongitude())
                .map(this::saveForecastData)
                .map(this::generateCsvFile);
    }

    @Transactional
    public ForecastRequest saveForecastData(ForecastSourceApiResponse apiResponse) {
        log.info("API response: " + apiResponse);

        ForecastRequest forecastRequest = forecastRequestRepository.save(
                ForecastRequest.builder()
                    .latitude(apiResponse.getLatitude())
                    .longitude(apiResponse.getLongitude())
                    .build()
        );

        forecastResponseDataRepository.save(
                ForecastResponseDataRecord.builder()
                        .temperature(apiResponse.getCurrentWeather().getTemperature())
                        .windSpeed(apiResponse.getCurrentWeather().getWindSpeed())
                        .windDirection(apiResponse.getCurrentWeather().getWindDirection())
                        .localTimestamp(LocalDateTime.now())
                        .utcTimestamp(LocalDateTime.now(ZoneOffset.UTC))
                        .forecastRequest(forecastRequest)
                        .build()
        );

        return forecastRequest;
    }

    private Resource generateCsvFile(ForecastRequest forecastRequest) {
        log.info("Creating CSV file...");

        try (ByteArrayInOutStream csvStream  = new ByteArrayInOutStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(csvStream);
            CSVWriter csvWriter = new CSVWriter(streamWriter,
                    ICSVWriter.DEFAULT_SEPARATOR,
                    ICSVWriter.NO_QUOTE_CHARACTER,
                    ICSVWriter.NO_ESCAPE_CHARACTER,
                    ICSVWriter.DEFAULT_LINE_END);) {

            csvWriter.writeNext(CSV_COLUMNS);

            forecastRequestRepository.findByLatitudeAndLongitude(forecastRequest.getLatitude(), forecastRequest.getLongitude())
                    .stream()
                    .map(forecastResponseDataRepository::findByForecastRequest)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(detail -> buildCsvRow(forecastRequest, detail))
                    .forEach(csvWriter::writeNext);

            streamWriter.flush();

            return new InputStreamResource(csvStream.getInputStream());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] buildCsvRow(ForecastRequest request, ForecastResponseDataRecord response) {
        return new String[] {
                response.getLocalTimestamp().toString(),
                response.getUtcTimestamp().toString(),
                Double.toString(request.getLatitude()),
                Double.toString(request.getLongitude()),
                Double.toString(response.getTemperature()),
                Double.toString(response.getWindSpeed()),
                Integer.toString(response.getWindDirection())
        };
    }
}
