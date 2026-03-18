package ru.tbank.practicum.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.tbank.practicum.dto.WeatherProperties;

@Configuration
@EnableConfigurationProperties(WeatherProperties.class)
public class RestClientsConfig {

    @Bean
    RestClient weatherRestClient(WeatherProperties props) {
        return RestClient.builder().baseUrl(props.baseUrl()).build();
    }
}
