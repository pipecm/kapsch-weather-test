package net.kapsch.weather;

import net.kapsch.weather.dto.ForecastRequestDto;
import net.kapsch.weather.exception.ForecastAppErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient(timeout = "3600000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ForecastApplicationTests extends ForecastApplicationBaseTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void whenRequestingCsvFileThenCsvFileIsReturnedOK() {
		ForecastRequestDto forecastRequestDto = ForecastRequestDto.builder()
				.latitude(LATITUDE_SCL)
				.longitude(LONGITUDE_SCL)
				.build();

		webTestClient
				.post()
				.uri(FORECAST_ENDPOINT)
				.body(BodyInserters.fromValue(forecastRequestDto))
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(CONTENT_TYPE_TEXT_CSV)
				.returnResult(Resource.class)
				.getResponseBody()
				.subscribe(resource -> assertThat(csvRowCount(resource)).isEqualTo(2));
	}

	@Test
	void whenRequestingCsvFileThenError() {
		webTestClient
				.post()
				.uri(FORECAST_ENDPOINT)
				.body(BodyInserters.fromValue(ForecastRequestDto.builder().build()))
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.returnResult(ForecastAppErrorResponse.class)
				.getResponseBody()
				.subscribe(errorResponse -> assertErrorResponse(errorResponse, HttpStatus.BAD_REQUEST, BAD_REQUEST_EXCEPTION_MSG));
	}
}
