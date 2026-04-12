package ru.tbank.practicum.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaTopicConfig {

    @Bean
    public NewTopic blindsCommandsTopic() {
        return new NewTopic("blinds.commands", 1, (short) 1);
    }

    @Bean
    public NewTopic radiatorCommandsTopic() {
        return new NewTopic("radiator.commands", 1, (short) 1);
    }

    @Bean
    public NewTopic devicesEventsTopic() {
        return new NewTopic("devices.events", 1, (short) 1);
    }
}