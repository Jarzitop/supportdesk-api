package com.joserojas.supportdesk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.dto.request.AssignTicketRequest;
import com.joserojas.supportdesk.dto.request.CreateTicketRequest;
import com.joserojas.supportdesk.dto.request.UpdateTicketStatusRequest;
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

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final SlaCalculator slaCalculator;
    private final TicketHistoryService ticketHistoryService;

    public TicketService(
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository,
            SlaCalculator slaCalculator,
            TicketHistoryService ticketHistoryService) {
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
        this.slaCalculator = slaCalculator;
        this.ticketHistoryService = ticketHistoryService;
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
            tickets = ticketRepository.findByStatusAndPriorityOrderByCreatedAtDescIdDesc(status, priority);
        } else if (status != null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtDescIdDesc(status);
        } else if (priority != null) {
            tickets = ticketRepository.findByPriorityOrderByCreatedAtDescIdDesc(priority);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDescIdDesc();
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

    @Transactional
    public TicketResponse assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket with id " + ticketId + " was not found"));

        AppUser assignedAgent = appUserRepository.findById(request.assignedAgentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + request.assignedAgentId() + " was not found"));

        if (assignedAgent.getRole() != Role.SUPPORT_AGENT) {
            throw new InvalidTicketOperationException(
                    "User with id " + request.assignedAgentId() + " is not a support agent");
        }

        AppUser previousAssignedAgent = ticket.getAssignedAgent();
        if (previousAssignedAgent != null
                && Objects.equals(previousAssignedAgent.getId(), assignedAgent.getId())) {
            return toResponse(ticket);
        }

        String oldValue = previousAssignedAgent == null ? null : previousAssignedAgent.getId().toString();
        String newValue = assignedAgent.getId().toString();
        ticket.setAssignedAgent(assignedAgent);
        ticket.markUpdated();

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketHistoryService.recordChange(savedTicket, null, "assignedAgent", oldValue, newValue);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket with id " + ticketId + " was not found"));

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.status();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidTicketOperationException(
                    "Cannot change ticket status from " + currentStatus + " to " + newStatus);
        }

        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket.markUpdated();
        Ticket savedTicket = ticketRepository.save(ticket);
        ticketHistoryService.recordChange(
                savedTicket,
                null,
                "status",
                currentStatus.name(),
                newStatus.name());

        return toResponse(savedTicket);
    }

    private boolean isValidStatusTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        return switch (currentStatus) {
            case OPEN -> newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.RESOLVED;
            case IN_PROGRESS -> newStatus == TicketStatus.RESOLVED;
            case RESOLVED -> newStatus == TicketStatus.CLOSED;
            case CLOSED -> false;
        };
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
