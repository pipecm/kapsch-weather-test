package net.kapsch.weather.repository;

import net.kapsch.weather.entity.ForecastRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForecastRequestRepository extends JpaRepository<ForecastRequest, UUID> {

}
