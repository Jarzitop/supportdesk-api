package com.joserojas.supportdesk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.dto.response.TicketHistoryResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.entity.TicketHistory;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.TicketHistoryRepository;
import com.joserojas.supportdesk.repository.TicketRepository;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketRepository ticketRepository;

    public TicketHistoryService(
            TicketHistoryRepository ticketHistoryRepository,
            TicketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getHistoryByTicketId(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket with id " + ticketId + " was not found");
        }

        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void recordChange(
            Ticket ticket,
            AppUser changedBy,
            String fieldName,
            String oldValue,
            String newValue) {
        TicketHistory history = new TicketHistory(ticket, changedBy, fieldName, oldValue, newValue);
        ticketHistoryRepository.save(history);
    }

    private TicketHistoryResponse toResponse(TicketHistory history) {
        AppUser changedBy = history.getChangedBy();

        return new TicketHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                changedBy == null ? null : changedBy.getId(),
                changedBy == null ? null : changedBy.getFullName(),
                history.getFieldName(),
                history.getOldValue(),
                history.getNewValue(),
                history.getChangedAt());
    }
}
