package com.joserojas.supportdesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.joserojas.supportdesk.dto.request.CreateTicketCommentRequest;
import com.joserojas.supportdesk.dto.response.PageResponse;
import com.joserojas.supportdesk.dto.response.TicketCommentResponse;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.service.TicketCommentService;

@WebMvcTest(TicketCommentController.class)
class TicketCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketCommentService ticketCommentService;

    @Test
    void addCommentReturnsCreatedComment() throws Exception {
        when(ticketCommentService.addComment(eq(10L), any(CreateTicketCommentRequest.class)))
                .thenReturn(commentResponse());

        mockMvc.perform(post("/api/v1/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": 2,
                                  "content": "I am investigating this issue."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.ticketId").value(10))
                .andExpect(jsonPath("$.authorName").value("Sam Lee"));

        verify(ticketCommentService).addComment(eq(10L), any(CreateTicketCommentRequest.class));
    }

    @Test
    void getCommentsReturnsRequestedPageOfTicketComments() throws Exception {
        when(ticketCommentService.getCommentsByTicketId(10L, 0, 2))
                .thenReturn(new PageResponse<>(
                        List.of(commentResponse(), commentResponse()), 0, 2, 3, 2, true, false));

        mockMvc.perform(get("/api/v1/tickets/10/comments").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(20))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(ticketCommentService).getCommentsByTicketId(10L, 0, 2);
    }

    @Test
    void getCommentsReturnsNotFoundWhenTicketDoesNotExist() throws Exception {
        when(ticketCommentService.getCommentsByTicketId(99L, 0, 20))
                .thenThrow(new ResourceNotFoundException("Ticket with id 99 was not found"));

        mockMvc.perform(get("/api/v1/tickets/99/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ticket with id 99 was not found"));
    }

    @Test
    void addCommentReturnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": 0,
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void addCommentReturnsNotFoundForUnknownTicket() throws Exception {
        when(ticketCommentService.addComment(eq(99L), any(CreateTicketCommentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Ticket with id 99 was not found"));

        mockMvc.perform(post("/api/v1/tickets/99/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorId": 2,
                                  "content": "I am investigating this issue."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ticket with id 99 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private TicketCommentResponse commentResponse() {
        return new TicketCommentResponse(
                20L,
                10L,
                2L,
                "Sam Lee",
                "I am investigating this issue.",
                LocalDateTime.of(2026, 6, 28, 14, 0));
    }
}
