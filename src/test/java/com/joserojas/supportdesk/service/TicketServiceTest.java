package com.joserojas.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.joserojas.supportdesk.dto.request.AssignTicketRequest;
import com.joserojas.supportdesk.dto.request.CreateTicketRequest;
import com.joserojas.supportdesk.dto.response.TicketResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.Role;
import com.joserojas.supportdesk.enums.TicketStatus;
import com.joserojas.supportdesk.exception.InvalidTicketOperationException;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.AppUserRepository;
import com.joserojas.supportdesk.repository.TicketRepository;
import com.joserojas.supportdesk.util.SlaCalculator;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private SlaCalculator slaCalculator;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createsOpenTicketWithCalculatedDueAt() {
        AppUser requester = new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER);
        LocalDateTime dueAt = LocalDateTime.of(2026, 6, 29, 10, 0);
        CreateTicketRequest request = new CreateTicketRequest(
                "Cannot sign in",
                "The login page rejects valid credentials",
                Priority.HIGH,
                1L);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(slaCalculator.calculateDueAt(any(LocalDateTime.class), eq(Priority.HIGH))).thenReturn(dueAt);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.createTicket(request);

        assertEquals(TicketStatus.OPEN, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals("Alex Rivera", response.requesterName());
        assertEquals(dueAt, response.dueAt());
        assertNull(response.assignedAgentId());
        assertNull(response.assignedAgentName());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void rejectsTicketWhenRequesterDoesNotExist() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Cannot sign in",
                "The login page rejects valid credentials",
                Priority.HIGH,
                99L);
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.createTicket(request));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void filtersTicketsByStatusAndPriorityTogether() {
        when(ticketRepository.findByStatusAndPriority(TicketStatus.OPEN, Priority.CRITICAL))
                .thenReturn(List.of());

        List<TicketResponse> responses = ticketService.getAllTickets(TicketStatus.OPEN, Priority.CRITICAL);

        assertEquals(List.of(), responses);
        verify(ticketRepository).findByStatusAndPriority(TicketStatus.OPEN, Priority.CRITICAL);
    }

    @Test
    void rejectsUnknownTicketId() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketById(99L));
    }

    @Test
    void assignsTicketToSupportAgent() {
        AppUser requester = new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER);
        AppUser supportAgent = new AppUser("Sam Lee", "sam@example.com", Role.SUPPORT_AGENT);
        Ticket ticket = new Ticket("Cannot sign in", "Valid credentials are rejected", Priority.HIGH, requester);
        AssignTicketRequest request = new AssignTicketRequest(2L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(supportAgent));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketResponse response = ticketService.assignTicket(10L, request);

        assertEquals("Sam Lee", response.assignedAgentName());
        assertEquals(TicketStatus.OPEN, response.status());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void rejectsAssignmentWhenUserDoesNotExist() {
        AppUser requester = new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER);
        Ticket ticket = new Ticket("Cannot sign in", "Valid credentials are rejected", Priority.HIGH, requester);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.assignTicket(10L, new AssignTicketRequest(99L)));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void rejectsAssignmentToRequester() {
        AppUser requester = new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER);
        Ticket ticket = new Ticket("Cannot sign in", "Valid credentials are rejected", Priority.HIGH, requester);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThrows(
                InvalidTicketOperationException.class,
                () -> ticketService.assignTicket(10L, new AssignTicketRequest(1L)));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
