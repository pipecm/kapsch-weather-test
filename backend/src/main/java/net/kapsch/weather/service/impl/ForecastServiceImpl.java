package net.kapsch.weather.service.impl;

import lombok.AllArgsConstructor;
import net.kapsch.weather.repository.ForecastRequestRepository;
import net.kapsch.weather.repository.ForecastResponseDataRepository;
import net.kapsch.weather.service.ForecastService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private final ForecastRequestRepository forecastRequestRepository;
    private final ForecastResponseDataRepository forecastResponseDataRepository;

    @Override
    public void getAllData(double latitude, double longitude) {

    }
}
