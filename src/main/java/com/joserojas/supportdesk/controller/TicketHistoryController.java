package com.joserojas.supportdesk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.joserojas.supportdesk.dto.response.TicketHistoryResponse;
import com.joserojas.supportdesk.service.TicketHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/history")
@Tag(name = "Ticket History", description = "Ticket history endpoints")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    @GetMapping
    @Operation(summary = "View a ticket's change history")
    public ResponseEntity<List<TicketHistoryResponse>> getHistoryByTicketId(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketHistoryService.getHistoryByTicketId(ticketId));
    }
}
