package ru.tbank.practicum.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.GeocodingLocation;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;

@Service
public class WeatherClient {
    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);

    private final RestClient restClient;
    private final WeatherProperties props;

    public WeatherClient(RestClient restClient, WeatherProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

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
                    throw new RuntimeException("Rate limit from weather API (429)");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Weather API is unavailable: " + res.getStatusCode());
                })
                .body(OpenWeatherResponse.class);

        log.debug("Weather response body: {}", responseBody);
        return responseBody;
    }

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
                    throw new RuntimeException("Rate limit from geocoding API (429)");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Geocoding API is unavailable: " + res.getStatusCode());
                })
                .body(new ParameterizedTypeReference<List<GeocodingLocation>>() {});

        if (locations == null || locations.isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }

        return locations.getFirst();
    }
}
