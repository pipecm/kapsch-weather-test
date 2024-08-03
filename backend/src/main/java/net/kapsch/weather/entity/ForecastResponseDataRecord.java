package net.kapsch.weather.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name =  "local_request_dt")
    private LocalDateTime localTimestamp;

    @Column(name = "utc_request_dt")
    private LocalDateTime utcTimestamp;

    @ManyToOne
    @JoinColumn(name = "forecast_request_id", nullable = false)
    @ToString.Exclude
    private ForecastRequest forecastRequest;
}
