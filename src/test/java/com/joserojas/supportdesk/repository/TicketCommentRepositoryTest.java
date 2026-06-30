package com.joserojas.supportdesk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.entity.TicketComment;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.Role;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketCommentRepositoryTest {

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsTicketCommentsByCreatedAtAscending() {
        AppUser author = appUserRepository.saveAndFlush(
                new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER));
        Ticket ticket = ticketRepository.saveAndFlush(
                new Ticket("Cannot sign in", "Valid credentials are rejected", Priority.HIGH, author));
        TicketComment later = ticketCommentRepository.saveAndFlush(
                new TicketComment(ticket, author, "Later comment"));
        TicketComment earlier = ticketCommentRepository.saveAndFlush(
                new TicketComment(ticket, author, "Earlier comment"));

        setCreatedAt(later.getId(), LocalDateTime.of(2026, 6, 29, 13, 0));
        setCreatedAt(earlier.getId(), LocalDateTime.of(2026, 6, 29, 12, 0));
        entityManager.clear();

        List<TicketComment> comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());

        assertEquals(
                List.of("Earlier comment", "Later comment"),
                comments.stream().map(TicketComment::getContent).toList());
    }

    private void setCreatedAt(Long commentId, LocalDateTime createdAt) {
        entityManager.createQuery("""
                        update TicketComment comment
                        set comment.createdAt = :createdAt
                        where comment.id = :commentId
                        """)
                .setParameter("createdAt", createdAt)
                .setParameter("commentId", commentId)
                .executeUpdate();
    }
}
