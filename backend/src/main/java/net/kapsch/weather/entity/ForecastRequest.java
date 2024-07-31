package net.kapsch.weather.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "forecast_request")
public class ForecastRequest {

    @Id
    @Column(name = "request_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID requestId;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column(name =  "local_timestamp")
    private LocalDateTime localTimestamp;

    @Column(name = "utc_timestamp")
    private LocalDateTime utcTimestamp;

    @OneToMany(mappedBy = "forecastRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ForecastResponseDataRecord> responseData;
}
