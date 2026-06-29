package com.joserojas.supportdesk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.joserojas.supportdesk.dto.response.TicketHistoryResponse;
import com.joserojas.supportdesk.service.TicketHistoryService;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/history")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<TicketHistoryResponse>> getHistoryByTicketId(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketHistoryService.getHistoryByTicketId(ticketId));
    }
}
