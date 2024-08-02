package net.kapsch.weather.repository;

import net.kapsch.weather.entity.ForecastRequest;
import net.kapsch.weather.entity.ForecastResponseDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForecastResponseDataRepository extends JpaRepository<ForecastResponseDataRecord, UUID> {
    Optional<ForecastResponseDataRecord> findByForecastRequest(ForecastRequest forecastRequest);
}
