package ru.tbank.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.tbank.practicum.dto.OpenRouterProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableKafka
public class SpringTbankApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringTbankApplication.class, args);
    }
}
