package ru.tbank.practicum.dto.telegram;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.Weather;

@Component
public class TelegramMessageFormatter {

    public String buildWeatherMessage(Weather weather) {
        if (weather == null) {
            return "Погода пока недоступна.";
        }

        return """
                Текущая погода

                Город: %s
                Температура: %s °C
                Ощущается как: %s °C
                Описание: %s
                Влажность: %s %%
                Ветер: %s м/с
                """.formatted(
                        safe(weather.getName()),
                        weather.getTemp(),
                        weather.getFeelsLike(),
                        safe(weather.getDescription()),
                        weather.getHumidity(),
                        weather.getWindSpeed());
    }

    public String buildStatusMessage(Weather weather, List<Radiator> radiators, List<Blinds> blinds) {
        StringBuilder sb = new StringBuilder();

        sb.append("Статус умного дома\n\n");

        if (weather != null) {
            sb.append("Погода: ")
                    .append(safe(weather.getName()))
                    .append(", ")
                    .append(weather.getTemp())
                    .append(" °C, ")
                    .append(safe(weather.getDescription()))
                    .append("\n\n");
        } else {
            sb.append("Погода: нет данных\n\n");
        }

        sb.append("Радиаторы:\n");
        if (radiators == null || radiators.isEmpty()) {
            sb.append("— нет радиаторов\n");
        } else {
            for (Radiator radiator : radiators) {
                sb.append("— ")
                        .append(radiator.getId())
                        .append(": ")
                        .append(radiator.getTemp())
                        .append(" °C, online=")
                        .append(radiator.getIsOnline())
                        .append(", broken=")
                        .append(radiator.getIsBroken())
                        .append("\n");
            }
        }

        sb.append("\nЖалюзи:\n");
        if (blinds == null || blinds.isEmpty()) {
            sb.append("— нет жалюзи\n");
        } else {
            for (Blinds blind : blinds) {
                sb.append("— ")
                        .append(blind.getId())
                        .append(": ")
                        .append(blind.getState())
                        .append(", online=")
                        .append(blind.getIsOnline())
                        .append(", broken=")
                        .append(blind.getIsBroken())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
