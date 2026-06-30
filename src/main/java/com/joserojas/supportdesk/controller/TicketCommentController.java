package com.joserojas.supportdesk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joserojas.supportdesk.dto.request.CreateTicketCommentRequest;
import com.joserojas.supportdesk.dto.response.PageResponse;
import com.joserojas.supportdesk.dto.response.TicketCommentResponse;
import com.joserojas.supportdesk.service.TicketCommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments", description = "Ticket comment endpoints")
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService) {
        this.ticketCommentService = ticketCommentService;
    }

    @PostMapping
    @Operation(summary = "Add a comment to a ticket")
    public ResponseEntity<TicketCommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateTicketCommentRequest request) {
        TicketCommentResponse response = ticketCommentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List a ticket's comments")
    public ResponseEntity<PageResponse<TicketCommentResponse>> getCommentsByTicketId(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ticketCommentService.getCommentsByTicketId(ticketId, page, size));
    }
}
