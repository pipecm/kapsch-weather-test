package net.kapsch.weather.entity;

import jakarta.persistence.*;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "forecast_response_data_record")
public class ForecastResponseDataRecord {

    @Id
    @Column(name = "record_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column
    private double temperature;

    @Column
    private double windSpeed;

    @Column
    private int windDirection;

    @ManyToOne
    @JoinColumn(name = "forecast_request_id", nullable = false)
    @ToString.Exclude
    private ForecastRequest forecastRequest;
}
