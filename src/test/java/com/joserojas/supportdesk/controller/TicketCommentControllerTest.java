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
import com.joserojas.supportdesk.dto.response.TicketCommentResponse;
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
    void getCommentsReturnsTicketComments() throws Exception {
        when(ticketCommentService.getCommentsByTicketId(10L)).thenReturn(List.of(commentResponse()));

        mockMvc.perform(get("/api/v1/tickets/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].content").value("I am investigating this issue."));

        verify(ticketCommentService).getCommentsByTicketId(10L);
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
