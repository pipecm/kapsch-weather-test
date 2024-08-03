package net.kapsch.weather.service.impl;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kapsch.weather.client.ForecastSourceApiClient;
import net.kapsch.weather.client.response.ForecastSourceApiResponse;
import net.kapsch.weather.dto.ForecastCsvFileDto;
import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.entity.ForecastRequest;
import net.kapsch.weather.entity.ForecastResponseDataRecord;
import net.kapsch.weather.exception.ForecastAppException;
import net.kapsch.weather.repository.ForecastRequestRepository;
import net.kapsch.weather.repository.ForecastResponseDataRepository;
import net.kapsch.weather.service.ForecastService;
import net.kapsch.weather.util.ByteArrayInOutStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class ForecastServiceImpl implements ForecastService {
    private static final String HEADER_LOCAL_TIMESTAMP = "Local timestamp";
    private static final String HEADER_UTC_TIMESTAMP = "UTC timestamp";
    private static final String HEADER_LATITUDE = "Latitude";
    private static final String HEADER_LONGITUDE = "Longitude";
    private static final String HEADER_TEMPERATURE = "Temperature (%s)";
    private static final String HEADER_WIND_SPEED = "Wind speed (%s)";
    private static final String HEADER_WIND_DIRECTION = "Wind direction (%s)";

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
    public ForecastCsvFileDto saveForecastData(ForecastSourceApiResponse apiResponse) {
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

        return ForecastCsvFileDto.builder()
                .latitude(apiResponse.getLatitude())
                .longitude(apiResponse.getLongitude())
                .temperatureUnit(apiResponse.getCurrentWeatherUnits().getTemperature())
                .windSpeedUnit(apiResponse.getCurrentWeatherUnits().getWindSpeed())
                .windDirectionUnit(apiResponse.getCurrentWeatherUnits().getWindDirection())
                .build();
    }

    private Resource generateCsvFile(ForecastCsvFileDto forecastCsvFileDto) {
        log.info("Creating CSV file...");

        try (ByteArrayInOutStream csvStream  = new ByteArrayInOutStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(csvStream);
            CSVWriter csvWriter = new CSVWriter(streamWriter,
                    ICSVWriter.DEFAULT_SEPARATOR,
                    ICSVWriter.NO_QUOTE_CHARACTER,
                    ICSVWriter.NO_ESCAPE_CHARACTER,
                    ICSVWriter.DEFAULT_LINE_END);) {

            csvWriter.writeNext(buildCsvHeader(forecastCsvFileDto));

            forecastRequestRepository.findByLatitudeAndLongitude(forecastCsvFileDto.getLatitude(), forecastCsvFileDto.getLongitude())
                    .stream()
                    .map(forecastResponseDataRepository::findByForecastRequest)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(detail -> buildCsvRow(forecastCsvFileDto, detail))
                    .forEach(csvWriter::writeNext);

            streamWriter.flush();

            return new InputStreamResource(csvStream.getInputStream());
        }
        catch (Exception e) {
            throw new ForecastAppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String[] buildCsvHeader(ForecastCsvFileDto forecastCsvFileDto) {
        return new String[] {
                HEADER_LOCAL_TIMESTAMP,
                HEADER_UTC_TIMESTAMP,
                HEADER_LATITUDE,
                HEADER_LONGITUDE,
                String.format(HEADER_TEMPERATURE, forecastCsvFileDto.getTemperatureUnit()),
                String.format(HEADER_WIND_SPEED, forecastCsvFileDto.getWindSpeedUnit()),
                String.format(HEADER_WIND_DIRECTION, forecastCsvFileDto.getWindDirectionUnit())
        };
    }

    private String[] buildCsvRow(ForecastCsvFileDto request, ForecastResponseDataRecord response) {
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
