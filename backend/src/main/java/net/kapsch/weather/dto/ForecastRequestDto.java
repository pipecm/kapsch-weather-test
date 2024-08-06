package net.kapsch.weather.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastRequestDto {

    @JsonProperty("request_id")
    private UUID requestId;

    private Double latitude;

    private Double longitude;

}
