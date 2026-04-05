package ru.tbank.practicum.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.RadiatorRuleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadiatorRuleServiceTest {

    @Mock
    private RadiatorRuleRepository radiatorRuleRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private RadiatorRuleService radiatorRuleService;

    @DisplayName("Должен вернуть правило по id")
    @Test
    void shouldReturnRadiatorRuleById() {
        RadiatorRule radiatorRule = createRadiatorRule(
                1L,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        when(radiatorRuleRepository.findById(1L)).thenReturn(Optional.of(radiatorRule));

        RadiatorRule result = radiatorRuleService.getById(1L);

        assertSame(radiatorRule, result);
        assertEquals(1L, result.getId());
        verify(radiatorRuleRepository).findById(1L);
    }

    @DisplayName("Должен вернуть все правила")
    @Test
    void shouldReturnAllRadiatorRules() {
        RadiatorRule rule1 = createRadiatorRule(
                1L,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        RadiatorRule rule2 = createRadiatorRule(
                2L,
                10L,
                new BigDecimal("11"),
                new BigDecimal("20"),
                new BigDecimal("24"),
                true
        );

        List<RadiatorRule> rules = List.of(rule1, rule2);

        when(radiatorRuleRepository.findAll()).thenReturn(rules);

        List<RadiatorRule> result = radiatorRuleService.getAll();

        assertSame(rules, result);
        assertEquals(2, result.size());
        verify(radiatorRuleRepository).findAll();
    }

    @DisplayName("Должен вернуть активные правила по radiatorId")
    @Test
    void shouldReturnActiveRulesByRadiatorId() {
        Long radiatorId = 10L;

        RadiatorRule rule1 = createRadiatorRule(
                1L,
                radiatorId,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        RadiatorRule rule2 = createRadiatorRule(
                2L,
                radiatorId,
                new BigDecimal("11"),
                new BigDecimal("20"),
                new BigDecimal("24"),
                true
        );

        List<RadiatorRule> rules = List.of(rule1, rule2);

        when(radiatorRuleRepository.findByRadiatorIdAndEnabledTrue(radiatorId)).thenReturn(rules);

        List<RadiatorRule> result = radiatorRuleService.getActiveRulesByRadiatorId(radiatorId);

        assertSame(rules, result);
        assertEquals(2, result.size());
        verify(radiatorRuleRepository).findByRadiatorIdAndEnabledTrue(radiatorId);
    }

    @DisplayName("Должен вернуть все правила по radiatorId")
    @Test
    void shouldReturnRulesByRadiatorId() {
        Long radiatorId = 10L;

        RadiatorRule rule1 = createRadiatorRule(
                1L,
                radiatorId,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        RadiatorRule rule2 = createRadiatorRule(
                2L,
                radiatorId,
                new BigDecimal("11"),
                new BigDecimal("20"),
                new BigDecimal("24"),
                false
        );

        List<RadiatorRule> rules = List.of(rule1, rule2);

        when(radiatorRuleRepository.findByRadiatorId(radiatorId)).thenReturn(rules);

        List<RadiatorRule> result = radiatorRuleService.getByRadiatorId(radiatorId);

        assertSame(rules, result);
        assertEquals(2, result.size());
        verify(radiatorRuleRepository).findByRadiatorId(radiatorId);
    }

    @DisplayName("Должен удалить правило и создать лог")
    @Test
    void shouldDeleteRuleById() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        RadiatorRule radiatorRule = createRadiatorRule(
                id,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        when(radiatorRuleRepository.findById(id)).thenReturn(Optional.of(radiatorRule));

        radiatorRuleService.deleteById(id, source);

        verify(radiatorRuleRepository).findById(id);
        verify(logService).createLog(
                DeviceType.RADIATOR,
                radiatorRule.getId(),
                LogStatus.WARNING,
                source,
                "DELETE_RADIATOR_RULE",
                "Radiator rule was deleted"
        );
        verify(radiatorRuleRepository).delete(radiatorRule);
    }

    @DisplayName("Должен сохранить правило и создать лог")
    @Test
    void shouldSaveRadiatorRule() {
        RadiatorRule radiatorRule = createRadiatorRule(
                null,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        RadiatorRule savedRule = createRadiatorRule(
                1L,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        when(radiatorRuleRepository.save(radiatorRule)).thenReturn(savedRule);

        RadiatorRule result = radiatorRuleService.save(radiatorRule);

        assertSame(savedRule, result);
        assertEquals(1L, result.getId());

        verify(radiatorRuleRepository).save(radiatorRule);
        verify(logService).createLog(
                DeviceType.RADIATOR,
                10L,
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "CREATE_RADIATOR_RULE",
                "Radiator rule was created successfully"
        );
    }

    @DisplayName("Должен обновить правило и создать лог")
    @Test
    void shouldUpdateRadiatorRule() {
        Long id = 1L;

        RadiatorRule radiatorRule = createRadiatorRule(
                id,
                10L,
                new BigDecimal("0"),
                new BigDecimal("10"),
                new BigDecimal("22"),
                true
        );

        BigDecimal newMin = new BigDecimal("5");
        BigDecimal newMax = new BigDecimal("15");
        BigDecimal newTarget = new BigDecimal("25");
        Boolean newEnabled = false;

        when(radiatorRuleRepository.findById(id)).thenReturn(Optional.of(radiatorRule));

        RadiatorRule result = radiatorRuleService.updateRadiatorRule(
                id,
                newMin,
                newMax,
                newTarget,
                newEnabled
        );

        assertSame(radiatorRule, result);
        assertEquals(newMin, result.getMinOutsideTemp());
        assertEquals(newMax, result.getMaxOutsideTemp());
        assertEquals(newTarget, result.getTargetRadiatorTemp());
        assertEquals(newEnabled, result.getEnabled());

        verify(radiatorRuleRepository).findById(id);
        verify(logService).createLog(
                DeviceType.RADIATOR,
                10L,
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "UPDATE_RADIATOR_RULE",
                "Radiator rule was updated successfully"
        );
    }

    private RadiatorRule createRadiatorRule(
            Long ruleId,
            Long radiatorId,
            BigDecimal minOutsideTemp,
            BigDecimal maxOutsideTemp,
            BigDecimal targetRadiatorTemp,
            Boolean enabled
    ) {
        Radiator radiator = new Radiator();
        radiator.setId(radiatorId);

        RadiatorRule rule = new RadiatorRule();
        rule.setId(ruleId);
        rule.setRadiator(radiator);
        rule.setMinOutsideTemp(minOutsideTemp);
        rule.setMaxOutsideTemp(maxOutsideTemp);
        rule.setTargetRadiatorTemp(targetRadiatorTemp);
        rule.setEnabled(enabled);

        return rule;
    }
}