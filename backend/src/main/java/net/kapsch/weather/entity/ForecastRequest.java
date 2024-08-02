package net.kapsch.weather.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    @ToString.Exclude
    @OneToMany(mappedBy = "forecastRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ForecastResponseDataRecord> responseData;
}
