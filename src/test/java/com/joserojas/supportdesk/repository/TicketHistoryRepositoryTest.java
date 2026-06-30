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
import com.joserojas.supportdesk.entity.TicketHistory;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.Role;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketHistoryRepositoryTest {

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsTicketHistoryByChangedAtAscending() {
        AppUser requester = appUserRepository.saveAndFlush(
                new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER));
        Ticket ticket = ticketRepository.saveAndFlush(
                new Ticket("Cannot sign in", "Valid credentials are rejected", Priority.HIGH, requester));
        TicketHistory later = ticketHistoryRepository.saveAndFlush(
                new TicketHistory(ticket, null, "status", "OPEN", "IN_PROGRESS"));
        TicketHistory earlier = ticketHistoryRepository.saveAndFlush(
                new TicketHistory(ticket, null, "assignedAgent", null, "2"));

        setChangedAt(later.getId(), LocalDateTime.of(2026, 6, 29, 13, 0));
        setChangedAt(earlier.getId(), LocalDateTime.of(2026, 6, 29, 12, 0));
        entityManager.clear();

        List<TicketHistory> history = ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticket.getId());

        assertEquals(
                List.of("assignedAgent", "status"),
                history.stream().map(TicketHistory::getFieldName).toList());
    }

    private void setChangedAt(Long historyId, LocalDateTime changedAt) {
        entityManager.createQuery("""
                        update TicketHistory history
                        set history.changedAt = :changedAt
                        where history.id = :historyId
                        """)
                .setParameter("changedAt", changedAt)
                .setParameter("historyId", historyId)
                .executeUpdate();
    }
}
