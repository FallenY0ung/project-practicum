package ru.tbank.practicum.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.OpenRouterChatRequest;
import ru.tbank.practicum.dto.OpenRouterChatResponse;
import ru.tbank.practicum.dto.OpenRouterProperties;
import ru.tbank.practicum.entity.Weather;

@Service
@Slf4j
public class WeatherAdviceService {

    private final RestClient restClient;
    private final OpenRouterProperties properties;
    private final Counter aiAdviceSuccessRequestsCounter;
    private final Counter aiAdviceFailedRequestsCounter;

    public WeatherAdviceService(OpenRouterProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("HTTP-Referer", properties.siteUrl())
                .defaultHeader("X-OpenRouter-Title", properties.siteName())
                .build();

        this.aiAdviceSuccessRequestsCounter = Counter.builder("openrouter.ai.requests")
                .description("Number of successful requests to OpenRouter AI for weather advice")
                .tag("status", "success")
                .register(meterRegistry);

        this.aiAdviceFailedRequestsCounter = Counter.builder("openrouter.ai.requests")
                .description("Number of failed requests to OpenRouter AI for weather advice")
                .tag("status", "error")
                .register(meterRegistry);
    }

    public String getAdvice(Weather weather) {
        if (weather == null) {
            return "Нет данных о погоде для получения рекомендации.";
        }

        String weatherJson = """
                {
                  "city": "%s",
                  "temperature": %s,
                  "feelsLike": %s,
                  "description": "%s",
                  "humidity": %s,
                  "windSpeed": %s
                }
                """.formatted(
                        safe(weather.getName()),
                        weather.getTemp(),
                        weather.getFeelsLike(),
                        safe(weather.getDescription()),
                        weather.getHumidity(),
                        weather.getWindSpeed());

        String prompt = """
                Ты помощник в приложении умного дома.
                На основе JSON с погодой дай короткий совет на русском языке:
                как одеться сегодня, нужна ли верхняя одежда, нужен ли зонт.
                Ответ должен быть простым, понятным, без воды, 2-4 предложения.

                JSON погоды:
                %s
                """.formatted(weatherJson);

        OpenRouterChatRequest request = new OpenRouterChatRequest(
                properties.model(),
                List.of(
                        new OpenRouterChatRequest.Message(
                                "system", "Ты даешь краткие и практичные рекомендации по одежде по погоде."),
                        new OpenRouterChatRequest.Message("user", prompt)));

        log.info("OpenRouter baseUrl={}", properties.baseUrl());
        log.info("OpenRouter model={}", properties.model());
        log.info(
                "OpenRouter apiKey present={}",
                properties.apiKey() != null && !properties.apiKey().isBlank());

        try {
            OpenRouterChatResponse response = restClient
                    .post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenRouterChatResponse.class);

            if (response == null
                    || response.choices() == null
                    || response.choices().isEmpty()
                    || response.choices().get(0).message() == null
                    || response.choices().get(0).message().content() == null) {
                return "Не удалось получить совет от ИИ.";
            }

            aiAdviceSuccessRequestsCounter.increment();
            return response.choices().get(0).message().content().trim();
        } catch (Exception e) {
            aiAdviceFailedRequestsCounter.increment();
            throw e;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}
