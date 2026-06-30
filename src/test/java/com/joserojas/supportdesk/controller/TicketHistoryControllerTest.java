package com.joserojas.supportdesk.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.joserojas.supportdesk.dto.response.PageResponse;
import com.joserojas.supportdesk.dto.response.TicketHistoryResponse;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.service.TicketHistoryService;

@WebMvcTest(TicketHistoryController.class)
class TicketHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketHistoryService ticketHistoryService;

    @Test
    void getHistoryReturnsRequestedPageOfTicketHistory() throws Exception {
        when(ticketHistoryService.getHistoryByTicketId(10L, 0, 2))
                .thenReturn(new PageResponse<>(
                        List.of(historyResponse(), historyResponse()), 0, 2, 3, 2, true, false));

        mockMvc.perform(get("/api/v1/tickets/10/history").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].ticketId").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(ticketHistoryService).getHistoryByTicketId(10L, 0, 2);
    }

    @Test
    void getHistoryRejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/10/history").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getHistoryRejectsZeroSize() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/10/history").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getHistoryReturnsNotFoundWhenTicketDoesNotExist() throws Exception {
        when(ticketHistoryService.getHistoryByTicketId(99L, 0, 20))
                .thenThrow(new ResourceNotFoundException("Ticket with id 99 was not found"));

        mockMvc.perform(get("/api/v1/tickets/99/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ticket with id 99 was not found"));
    }

    private TicketHistoryResponse historyResponse() {
        return new TicketHistoryResponse(
                30L,
                10L,
                null,
                null,
                "status",
                "OPEN",
                "IN_PROGRESS",
                LocalDateTime.of(2026, 6, 28, 15, 0));
    }
}
