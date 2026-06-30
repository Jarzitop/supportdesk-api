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
import com.joserojas.supportdesk.dto.request.UpdateTicketStatusRequest;
import com.joserojas.supportdesk.dto.response.PageResponse;
import com.joserojas.supportdesk.dto.response.TicketResponse;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;
import com.joserojas.supportdesk.exception.InvalidTicketOperationException;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
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
    void getAllTicketsReturnsPaginatedStructureWithDefaults() throws Exception {
        when(ticketService.getAllTickets(0, 20, null, null)).thenReturn(pageResponse());

        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(ticketService).getAllTickets(0, 20, null, null);
    }

    @Test
    void getAllTicketsUsesRequestedPageSize() throws Exception {
        when(ticketService.getAllTickets(0, 2, null, null))
                .thenReturn(new PageResponse<>(List.of(ticketResponse()), 0, 2, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/tickets").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(2));

        verify(ticketService).getAllTickets(0, 2, null, null);
    }

    @Test
    void getAllTicketsRejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request parameter validation failed"));
    }

    @Test
    void getAllTicketsRejectsZeroSize() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllTicketsRejectsSizeAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllTicketsFiltersByStatus() throws Exception {
        when(ticketService.getAllTickets(0, 20, TicketStatus.OPEN, null)).thenReturn(pageResponse());

        mockMvc.perform(get("/api/v1/tickets").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));

        verify(ticketService).getAllTickets(0, 20, TicketStatus.OPEN, null);
    }

    @Test
    void getAllTicketsFiltersByPriority() throws Exception {
        when(ticketService.getAllTickets(0, 20, null, Priority.HIGH)).thenReturn(pageResponse());

        mockMvc.perform(get("/api/v1/tickets").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));

        verify(ticketService).getAllTickets(0, 20, null, Priority.HIGH);
    }

    @Test
    void getAllTicketsFiltersByStatusAndPriority() throws Exception {
        when(ticketService.getAllTickets(0, 20, TicketStatus.OPEN, Priority.HIGH)).thenReturn(pageResponse());

        mockMvc.perform(get("/api/v1/tickets")
                        .param("status", "OPEN")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));

        verify(ticketService).getAllTickets(0, 20, TicketStatus.OPEN, Priority.HIGH);
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
    void getTicketByIdReturnsNotFoundForUnknownTicket() throws Exception {
        when(ticketService.getTicketById(99L))
                .thenThrow(new ResourceNotFoundException("Ticket with id 99 was not found"));

        mockMvc.perform(get("/api/v1/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ticket with id 99 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTicketReturnsNotFoundForUnknownRequester() throws Exception {
        when(ticketService.createTicket(any(CreateTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("User with id 99 was not found"));

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot sign in",
                                  "description": "The login page rejects valid credentials",
                                  "priority": "HIGH",
                                  "requesterId": 99
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id 99 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
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
    void createTicketReturnsBadRequestWhenRequesterIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot sign in",
                                  "description": "The login page rejects valid credentials",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("requesterId: Requester id is required"))
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

    @Test
    void assignTicketReturnsNotFoundForUnknownAgent() throws Exception {
        when(ticketService.assignTicket(eq(10L), any(AssignTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("User with id 99 was not found"));

        mockMvc.perform(patch("/api/v1/tickets/10/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignedAgentId": 99
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id 99 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateTicketStatusReturnsUpdatedTicket() throws Exception {
        TicketResponse updatedTicket = ticketWithStatus(TicketStatus.IN_PROGRESS);
        when(ticketService.updateTicketStatus(eq(10L), any(UpdateTicketStatusRequest.class)))
                .thenReturn(updatedTicket);

        mockMvc.perform(patch("/api/v1/tickets/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(ticketService).updateTicketStatus(eq(10L), any(UpdateTicketStatusRequest.class));
    }

    @Test
    void updateTicketStatusReturnsBadRequestForMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/tickets/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("status: Status is required"));
    }

    @Test
    void updateTicketStatusReturnsBadRequestForInvalidTransition() throws Exception {
        when(ticketService.updateTicketStatus(eq(10L), any(UpdateTicketStatusRequest.class)))
                .thenThrow(new InvalidTicketOperationException(
                        "Cannot change ticket status from OPEN to CLOSED"));

        mockMvc.perform(patch("/api/v1/tickets/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Cannot change ticket status from OPEN to CLOSED"))
                .andExpect(jsonPath("$.timestamp").exists());
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

    private PageResponse<TicketResponse> pageResponse() {
        return new PageResponse<>(List.of(ticketResponse()), 0, 20, 1, 1, true, true);
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

    private TicketResponse ticketWithStatus(TicketStatus status) {
        TicketResponse ticket = ticketResponse();
        return new TicketResponse(
                ticket.id(),
                ticket.title(),
                ticket.description(),
                status,
                ticket.priority(),
                ticket.requesterId(),
                ticket.requesterName(),
                ticket.assignedAgentId(),
                ticket.assignedAgentName(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.dueAt(),
                ticket.resolvedAt(),
                ticket.closedAt());
    }
}
