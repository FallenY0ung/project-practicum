package ru.tbank.practicum.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.GeocodingLocation;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.exceptionHandler.WeatherRateLimitException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    private final RestClient restClient;
    private final WeatherProperties props;
    public static final ParameterizedTypeReference<List<GeocodingLocation>> geocodinListType =
            new ParameterizedTypeReference<>() {};

    @Retryable(includes = WeatherRateLimitException.class, maxRetries = 10, delay = 1000)
    public OpenWeatherResponse getCurrent(String city) {
        log.info("Requesting geocoding for city {}", city);

        GeocodingLocation location = findLocation(city);

        log.info("Geocoding resolved city {} to lat={}, lon={}", city, location.lat(), location.lon());

        OpenWeatherResponse responseBody = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", location.lat())
                        .queryParam("lon", location.lon())
                        .queryParam("appid", props.apiKey())
                        .queryParam("units", "metric")
                        .queryParam("lang", "ru")
                        .build())
                .retrieve()
                .onStatus(s -> s.value() == 401, (req, res) -> {
                    throw new RuntimeException("Invalid OpenWeather API key");
                })
                .onStatus(s -> s.value() == 429, (req, res) -> {
                    throw new WeatherRateLimitException("Rate limit from weather API (429)");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    String errorBody = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
                    throw new RuntimeException(
                            "Weather API is unavailable: " + res.getStatusCode() + ", body=" + errorBody);
                })
                .body(OpenWeatherResponse.class);

        log.debug("Weather response body: {}", responseBody);
        return responseBody;
    }

    @Retryable(includes = WeatherRateLimitException.class, maxRetries = 10, delay = 1000)
    private GeocodingLocation findLocation(String city) {
        List<GeocodingLocation> locations = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/1.0/direct")
                        .queryParam("q", city)
                        .queryParam("limit", 1)
                        .queryParam("appid", props.apiKey())
                        .build())
                .retrieve()
                .onStatus(s -> s.value() == 401, (req, res) -> {
                    throw new RuntimeException("Invalid OpenWeather API key");
                })
                .onStatus(s -> s.value() == 429, (req, res) -> {
                    throw new WeatherRateLimitException("Rate limit from geocoding API (429)");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    String errorBody = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
                    throw new RuntimeException(
                            "Geocoding API is unavailable: " + res.getStatusCode() + ", body=" + errorBody);
                })
                .body(geocodinListType);

        if (locations == null || locations.isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }

        return locations.getFirst();
    }
}
