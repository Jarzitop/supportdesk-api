package com.joserojas.supportdesk.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.dto.request.CreateTicketRequest;
import com.joserojas.supportdesk.dto.response.TicketResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.AppUserRepository;
import com.joserojas.supportdesk.repository.TicketRepository;
import com.joserojas.supportdesk.util.SlaCalculator;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final SlaCalculator slaCalculator;

    public TicketService(
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository,
            SlaCalculator slaCalculator) {
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
        this.slaCalculator = slaCalculator;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        AppUser requester = appUserRepository.findById(request.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + request.requesterId() + " was not found"));

        Ticket ticket = new Ticket(request.title(), request.description(), request.priority(), requester);
        ticket.setDueAt(slaCalculator.calculateDueAt(LocalDateTime.now(), request.priority()));

        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketStatus status, Priority priority) {
        List<Ticket> tickets;

        if (status != null && priority != null) {
            tickets = ticketRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (priority != null) {
            tickets = ticketRepository.findByPriority(priority);
        } else {
            tickets = ticketRepository.findAll();
        }

        return tickets.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket with id " + id + " was not found"));

        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        AppUser requester = ticket.getRequester();
        AppUser assignedAgent = ticket.getAssignedAgent();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                requester.getId(),
                requester.getFullName(),
                assignedAgent == null ? null : assignedAgent.getId(),
                assignedAgent == null ? null : assignedAgent.getFullName(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getDueAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt());
    }
}
