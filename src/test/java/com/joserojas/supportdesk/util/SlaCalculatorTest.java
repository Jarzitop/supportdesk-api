package com.joserojas.supportdesk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.joserojas.supportdesk.enums.Priority;

class SlaCalculatorTest {

    private final SlaCalculator slaCalculator = new SlaCalculator();
    private final LocalDateTime startTime = LocalDateTime.of(2026, 6, 28, 10, 0);

    @Test
    void calculatesDueAtForEveryPriority() {
        assertEquals(startTime.plusHours(4), slaCalculator.calculateDueAt(startTime, Priority.CRITICAL));
        assertEquals(startTime.plusHours(24), slaCalculator.calculateDueAt(startTime, Priority.HIGH));
        assertEquals(startTime.plusHours(48), slaCalculator.calculateDueAt(startTime, Priority.MEDIUM));
        assertEquals(startTime.plusHours(72), slaCalculator.calculateDueAt(startTime, Priority.LOW));
    }
}
