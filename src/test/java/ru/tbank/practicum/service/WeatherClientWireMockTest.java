package ru.tbank.practicum.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.exceptionHandler.WeatherRateLimitException;

@WireMockTest
class WeatherClientWireMockTest {

    private static final String API_KEY = "test-api-key";

    private WeatherClient weatherClient;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        WeatherProperties props = mock(WeatherProperties.class);
        when(props.apiKey()).thenReturn(API_KEY);

        RestClient restClient =
                RestClient.builder().baseUrl(wmRuntimeInfo.getHttpBaseUrl()).build();

        weatherClient = new WeatherClient(restClient, props);
    }

    @Test
    @DisplayName("getCurrent должен получить геолокацию, затем погоду и корректно распарсить ответ")
    void getCurrent_shouldReturnWeather_whenApisRespondSuccessfully() {
        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("Saratov"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo(API_KEY))
                .willReturn(okJson("""
                        [
                          {
                            "name": "Saratov",
                            "lat": 51.530018,
                            "lon": 46.034683,
                            "country": "RU"
                          }
                        ]
                        """)));

        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("51.530018"))
                .withQueryParam("lon", equalTo("46.034683"))
                .withQueryParam("appid", equalTo(API_KEY))
                .withQueryParam("units", equalTo("metric"))
                .withQueryParam("lang", equalTo("ru"))
                .willReturn(okJson("""
                        {
                          "main": {
                            "temp": 11.48,
                            "feels_like": 9.96,
                            "pressure": 1023,
                            "humidity": 49
                          },
                          "weather": [
                            {
                              "description": "облачно с прояснениями"
                            }
                          ],
                          "wind": {
                            "speed": 0.92
                          },
                          "name": "Саратов"
                        }
                        """)));

        OpenWeatherResponse response = weatherClient.getCurrent("Saratov");

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Саратов");
        assertThat(response.main().temp()).isEqualByComparingTo("11.48");
        assertThat(response.main().feelsLike()).isEqualByComparingTo("9.96");
        assertThat(response.wind().speed()).isEqualByComparingTo(new BigDecimal("0.92"));
        assertThat(response.weather()).hasSize(1);
        assertThat(response.weather().getFirst().description()).isEqualTo("облачно с прояснениями");

        verify(getRequestedFor(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("Saratov"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo(API_KEY)));

        verify(getRequestedFor(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("51.530018"))
                .withQueryParam("lon", equalTo("46.034683"))
                .withQueryParam("appid", equalTo(API_KEY))
                .withQueryParam("units", equalTo("metric"))
                .withQueryParam("lang", equalTo("ru")));
    }

    @Test
    @DisplayName("getCurrent должен бросать ошибку, если город не найден")
    void getCurrent_shouldThrow_whenCityNotFound() {
        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("UnknownCity"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo(API_KEY))
                .willReturn(okJson("[]")));

        assertThatThrownBy(() -> weatherClient.getCurrent("UnknownCity"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("City not found: UnknownCity");

        verify(getRequestedFor(urlPathEqualTo("/geo/1.0/direct")).withQueryParam("q", equalTo("UnknownCity")));
        verify(0, getRequestedFor(urlPathEqualTo("/data/2.5/weather")));
    }

    @Test
    @DisplayName("getCurrent должен бросать WeatherRateLimitException при 429 от weather API")
    void getCurrent_shouldThrowRateLimitException_whenWeatherApiReturns429() {
        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("Saratov"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo(API_KEY))
                .willReturn(okJson("""
                        [
                          {
                            "name": "Saratov",
                            "lat": 51.530018,
                            "lon": 46.034683,
                            "country": "RU"
                          }
                        ]
                        """)));

        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "cod": 429,
                                  "message": "rate limit"
                                }
                                """)));

        assertThatThrownBy(() -> weatherClient.getCurrent("Saratov"))
                .isInstanceOf(WeatherRateLimitException.class)
                .hasMessage("Rate limit from weather API (429)");

        verify(getRequestedFor(urlPathEqualTo("/geo/1.0/direct")));
        verify(getRequestedFor(urlPathEqualTo("/data/2.5/weather")));
    }

    @Test
    @DisplayName("getCurrent должен бросать RuntimeException при 500 от weather API")
    void getCurrent_shouldThrow_whenWeatherApiReturns5xx() {
        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("Saratov"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo(API_KEY))
                .willReturn(okJson("""
                        [
                          {
                            "name": "Saratov",
                            "lat": 51.530018,
                            "lon": 46.034683,
                            "country": "RU"
                          }
                        ]
                        """)));

        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("weather service is down")));

        assertThatThrownBy(() -> weatherClient.getCurrent("Saratov"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Weather API is unavailable")
                .hasMessageContaining("500")
                .hasMessageContaining("weather service is down");
    }
}
