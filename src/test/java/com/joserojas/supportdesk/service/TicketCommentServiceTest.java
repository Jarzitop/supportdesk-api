package com.joserojas.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.joserojas.supportdesk.dto.request.CreateTicketCommentRequest;
import com.joserojas.supportdesk.dto.response.TicketCommentResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.entity.TicketComment;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.AppUserRepository;
import com.joserojas.supportdesk.repository.TicketCommentRepository;
import com.joserojas.supportdesk.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private Ticket ticket;

    @Mock
    private AppUser author;

    @InjectMocks
    private TicketCommentService ticketCommentService;

    @Test
    void addsCommentToTicket() {
        CreateTicketCommentRequest request = new CreateTicketCommentRequest(2L, "I am investigating this issue.");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(author));
        when(ticket.getId()).thenReturn(10L);
        when(author.getId()).thenReturn(2L);
        when(author.getFullName()).thenReturn("Sam Lee");
        when(ticketCommentRepository.save(any(TicketComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketCommentResponse response = ticketCommentService.addComment(10L, request);

        assertEquals(10L, response.ticketId());
        assertEquals(2L, response.authorId());
        assertEquals("Sam Lee", response.authorName());
        assertEquals("I am investigating this issue.", response.content());
        verify(ticketCommentRepository).save(any(TicketComment.class));
    }

    @Test
    void rejectsCommentWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketCommentService.addComment(
                        99L,
                        new CreateTicketCommentRequest(2L, "Comment")));

        verify(appUserRepository, never()).findById(any(Long.class));
        verify(ticketCommentRepository, never()).save(any(TicketComment.class));
    }

    @Test
    void rejectsCommentWhenAuthorDoesNotExist() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketCommentService.addComment(
                        10L,
                        new CreateTicketCommentRequest(99L, "Comment")));

        verify(ticketCommentRepository, never()).save(any(TicketComment.class));
    }

    @Test
    void listsCommentsInRepositoryOrderAfterVerifyingTicketExists() {
        TicketComment firstComment = new TicketComment(ticket, author, "First comment");
        TicketComment secondComment = new TicketComment(ticket, author, "Second comment");
        when(ticketRepository.existsById(10L)).thenReturn(true);
        when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(firstComment, secondComment));
        when(ticket.getId()).thenReturn(10L);
        when(author.getId()).thenReturn(2L);
        when(author.getFullName()).thenReturn("Sam Lee");

        List<TicketCommentResponse> responses = ticketCommentService.getCommentsByTicketId(10L);

        assertEquals(List.of("First comment", "Second comment"),
                responses.stream().map(TicketCommentResponse::content).toList());
        verify(ticketRepository).existsById(10L);
        verify(ticketCommentRepository).findByTicketIdOrderByCreatedAtAsc(10L);
    }
}
