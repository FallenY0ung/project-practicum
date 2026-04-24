package ru.tbank.practicum.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.service.TelegramBotService;
import ru.tbank.practicum.dto.telegram.TelegramGetUpdatesResponse;
import ru.tbank.practicum.dto.telegram.TelegramMessage;
import ru.tbank.practicum.dto.telegram.TelegramUpdate;
import ru.tbank.practicum.service.TelegramCommandService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramPollingScheduler {

    private final TelegramBotService telegramBotService;
    private final TelegramCommandService telegramCommandService;

    private Long lastProcessedUpdateId = null;



    @Scheduled(fixedDelay = 5000)
    public void pollUpdates() {
        try {
            log.info("Polling Telegram updates... start");

            Long offset = lastProcessedUpdateId == null ? null : lastProcessedUpdateId + 1;
            log.info("Polling with offset={}", offset);

            TelegramGetUpdatesResponse response = telegramBotService.getUpdates(offset);

            log.info("Telegram response received: {}", response);

            if (response == null || response.result() == null || response.result().isEmpty()) {
                log.info("No updates from Telegram");
                return;
            }

            for (TelegramUpdate update : response.result()) {
                log.info("Received update id={}", update.updateId());
                lastProcessedUpdateId = update.updateId();

                TelegramMessage message = update.message();
                if (message == null || message.chat() == null) {
                    log.info("Skipping update without message/chat");
                    continue;
                }

                log.info("Incoming text={}", message.text());

                String answer = telegramCommandService.handleCommand(message.text());
                log.info("Sending answer={}", answer);

                telegramBotService.sendMessage(message.chat().id(), answer);
            }
        } catch (Exception e) {
            log.warn("Failed to poll Telegram updates", e);
        }
    }

}