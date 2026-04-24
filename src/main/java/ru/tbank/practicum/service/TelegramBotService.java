package ru.tbank.practicum.service;

import java.net.http.HttpClient;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.telegram.TelegramGetUpdatesResponse;
import ru.tbank.practicum.dto.telegram.TelegramProperties;
import ru.tbank.practicum.dto.telegram.TelegramSendMessageRequest;

@Slf4j
@Service
public class TelegramBotService {

    private final RestClient restClient;
    private final TelegramProperties telegramProperties;

    public TelegramBotService(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.restClient = RestClient.builder()
                .baseUrl(telegramProperties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TelegramGetUpdatesResponse getUpdates(Long offset) {
        String uri = "/bot" + telegramProperties.botToken() + "/getUpdates?timeout=1";
        if (offset != null) {
            uri += "&offset=" + offset;
        }

        log.info("Telegram getUpdates called, offset={}", offset);

        TelegramGetUpdatesResponse response =
                restClient.get().uri(uri).retrieve().body(TelegramGetUpdatesResponse.class);

        log.info("Telegram getUpdates finished");
        return response;
    }

    public void sendMessage(Long chatId, String text) {
        restClient
                .post()
                .uri("/bot" + telegramProperties.botToken() + "/sendMessage")
                .body(new TelegramSendMessageRequest(chatId, text))
                .retrieve()
                .toBodilessEntity();
    }
}
