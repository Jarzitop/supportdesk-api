package com.joserojas.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.joserojas.supportdesk.dto.response.TicketHistoryResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.entity.TicketHistory;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.TicketHistoryRepository;
import com.joserojas.supportdesk.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketHistoryServiceTest {

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private Ticket ticket;

    @Mock
    private AppUser changedBy;

    @InjectMocks
    private TicketHistoryService ticketHistoryService;

    @Test
    void recordsTicketChange() {
        ticketHistoryService.recordChange(ticket, null, "status", "OPEN", "IN_PROGRESS");

        ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(ticketHistoryRepository).save(historyCaptor.capture());
        TicketHistory history = historyCaptor.getValue();
        assertEquals(ticket, history.getTicket());
        assertNull(history.getChangedBy());
        assertEquals("status", history.getFieldName());
        assertEquals("OPEN", history.getOldValue());
        assertEquals("IN_PROGRESS", history.getNewValue());
    }

    @Test
    void getsHistoryInRepositoryOrder() {
        TicketHistory assignment = new TicketHistory(ticket, changedBy, "assignedAgent", null, "2");
        TicketHistory status = new TicketHistory(ticket, null, "status", "OPEN", "IN_PROGRESS");
        when(ticketRepository.existsById(10L)).thenReturn(true);
        when(ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(10L))
                .thenReturn(List.of(assignment, status));
        when(ticket.getId()).thenReturn(10L);
        when(changedBy.getId()).thenReturn(1L);
        when(changedBy.getFullName()).thenReturn("Alex Rivera");

        List<TicketHistoryResponse> responses = ticketHistoryService.getHistoryByTicketId(10L);

        assertEquals(List.of("assignedAgent", "status"),
                responses.stream().map(TicketHistoryResponse::fieldName).toList());
        assertEquals("Alex Rivera", responses.getFirst().changedByName());
        assertNull(responses.get(1).changedById());
        verify(ticketRepository).existsById(10L);
        verify(ticketHistoryRepository).findByTicketIdOrderByChangedAtAsc(10L);
    }

    @Test
    void rejectsHistoryRequestWhenTicketDoesNotExist() {
        when(ticketRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketHistoryService.getHistoryByTicketId(99L));

        verify(ticketHistoryRepository, never())
                .findByTicketIdOrderByChangedAtAsc(any(Long.class));
    }
}
