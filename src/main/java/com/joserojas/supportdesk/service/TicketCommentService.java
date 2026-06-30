package com.joserojas.supportdesk.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.dto.request.CreateTicketCommentRequest;
import com.joserojas.supportdesk.dto.response.PageResponse;
import com.joserojas.supportdesk.dto.response.TicketCommentResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.entity.TicketComment;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.AppUserRepository;
import com.joserojas.supportdesk.repository.TicketCommentRepository;
import com.joserojas.supportdesk.repository.TicketRepository;

@Service
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;

    public TicketCommentService(
            TicketCommentRepository ticketCommentRepository,
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository) {
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public TicketCommentResponse addComment(Long ticketId, CreateTicketCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket with id " + ticketId + " was not found"));

        AppUser author = appUserRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + request.authorId() + " was not found"));

        TicketComment comment = new TicketComment(ticket, author, request.content());

        return toResponse(ticketCommentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketCommentResponse> getCommentsByTicketId(Long ticketId, int page, int size) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket with id " + ticketId + " was not found");
        }

        return PageResponse.from(ticketCommentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId, PageRequest.of(page, size))
                .map(this::toResponse));
    }

    private TicketCommentResponse toResponse(TicketComment comment) {
        return new TicketCommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
