package com.joserojas.supportdesk.util;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.joserojas.supportdesk.enums.Priority;

@Component
public class SlaCalculator {

    public LocalDateTime calculateDueAt(LocalDateTime startTime, Priority priority) {
        long responseHours = switch (priority) {
            case CRITICAL -> 4;
            case HIGH -> 24;
            case MEDIUM -> 48;
            case LOW -> 72;
        };

        return startTime.plusHours(responseHours);
    }
}
