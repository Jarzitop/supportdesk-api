package com.joserojas.supportdesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.joserojas.supportdesk.dto.request.AssignTicketRequest;
import com.joserojas.supportdesk.dto.request.CreateTicketRequest;
import com.joserojas.supportdesk.dto.response.TicketResponse;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;
import com.joserojas.supportdesk.exception.InvalidTicketOperationException;
import com.joserojas.supportdesk.service.TicketService;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void createTicketReturnsCreatedTicket() throws Exception {
        when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(ticketResponse());

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot sign in",
                                  "description": "The login page rejects valid credentials",
                                  "priority": "HIGH",
                                  "requesterId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(ticketService).createTicket(any(CreateTicketRequest.class));
    }

    @Test
    void getAllTicketsReturnsFilteredTickets() throws Exception {
        when(ticketService.getAllTickets(TicketStatus.OPEN, Priority.HIGH))
                .thenReturn(List.of(ticketResponse()));

        mockMvc.perform(get("/api/v1/tickets")
                        .param("status", "OPEN")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Cannot sign in"));

        verify(ticketService).getAllTickets(TicketStatus.OPEN, Priority.HIGH);
    }

    @Test
    void getTicketByIdReturnsTicket() throws Exception {
        when(ticketService.getTicketById(10L)).thenReturn(ticketResponse());

        mockMvc.perform(get("/api/v1/tickets/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.requesterName").value("Alex Rivera"));

        verify(ticketService).getTicketById(10L);
    }

    @Test
    void createTicketReturnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "",
                                  "priority": null,
                                  "requesterId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllTicketsReturnsBadRequestForInvalidEnumParameter() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("priority", "URGENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value 'URGENT' for parameter 'priority'"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void assignTicketReturnsUpdatedTicket() throws Exception {
        TicketResponse assignedTicket = assignedTicketResponse();
        when(ticketService.assignTicket(eq(10L), any(AssignTicketRequest.class)))
                .thenReturn(assignedTicket);

        mockMvc.perform(patch("/api/v1/tickets/10/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignedAgentId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.assignedAgentId").value(2))
                .andExpect(jsonPath("$.assignedAgentName").value("Sam Lee"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(ticketService).assignTicket(eq(10L), any(AssignTicketRequest.class));
    }

    @Test
    void assignTicketReturnsBadRequestForInvalidAgentId() throws Exception {
        mockMvc.perform(patch("/api/v1/tickets/10/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignedAgentId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("assignedAgentId: Assigned agent id must be positive"));
    }

    @Test
    void assignTicketReturnsBadRequestWhenUserIsNotSupportAgent() throws Exception {
        when(ticketService.assignTicket(eq(10L), any(AssignTicketRequest.class)))
                .thenThrow(new InvalidTicketOperationException("User with id 1 is not a support agent"));

        mockMvc.perform(patch("/api/v1/tickets/10/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignedAgentId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User with id 1 is not a support agent"));
    }

    private TicketResponse ticketResponse() {
        return new TicketResponse(
                10L,
                "Cannot sign in",
                "The login page rejects valid credentials",
                TicketStatus.OPEN,
                Priority.HIGH,
                1L,
                "Alex Rivera",
                null,
                null,
                LocalDateTime.of(2026, 6, 28, 12, 0),
                null,
                LocalDateTime.of(2026, 6, 29, 12, 0),
                null,
                null);
    }

    private TicketResponse assignedTicketResponse() {
        TicketResponse ticket = ticketResponse();
        return new TicketResponse(
                ticket.id(),
                ticket.title(),
                ticket.description(),
                ticket.status(),
                ticket.priority(),
                ticket.requesterId(),
                ticket.requesterName(),
                2L,
                "Sam Lee",
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.dueAt(),
                ticket.resolvedAt(),
                ticket.closedAt());
    }
}
