package com.joserojas.supportdesk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.Role;
import com.joserojas.supportdesk.enums.TicketStatus;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void filtersTicketsByStatusAndPriority() {
        AppUser requester = saveUser("Alex Rivera", "alex@example.com", Role.REQUESTER);
        AppUser supportAgent = saveUser("Sam Lee", "sam@example.com", Role.SUPPORT_AGENT);

        Ticket openHigh = saveTicket("Open high", Priority.HIGH, TicketStatus.OPEN, requester, null);
        Ticket inProgressHigh = saveTicket(
                "In-progress high",
                Priority.HIGH,
                TicketStatus.IN_PROGRESS,
                requester,
                supportAgent);
        Ticket openLow = saveTicket("Open low", Priority.LOW, TicketStatus.OPEN, requester, null);
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        assertEquals(
                List.of(openLow.getId(), openHigh.getId()),
                ids(ticketRepository.findByStatus(TicketStatus.OPEN, pageable)));
        assertEquals(
                List.of(inProgressHigh.getId(), openHigh.getId()),
                ids(ticketRepository.findByPriority(Priority.HIGH, pageable)));
        assertEquals(
                List.of(openHigh.getId()),
                ids(ticketRepository.findByStatusAndPriority(
                        TicketStatus.OPEN,
                        Priority.HIGH,
                        pageable)));
    }

    @Test
    void ordersTicketsByCreatedAtThenIdDescendingAndReturnsRelationships() {
        AppUser requester = saveUser("Alex Rivera", "requester@example.com", Role.REQUESTER);
        AppUser supportAgent = saveUser("Sam Lee", "agent@example.com", Role.SUPPORT_AGENT);
        Ticket first = saveTicket("First", Priority.MEDIUM, TicketStatus.OPEN, requester, null);
        Ticket second = saveTicket("Second", Priority.HIGH, TicketStatus.IN_PROGRESS, requester, supportAgent);
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 6, 29, 12, 0);

        entityManager.createQuery("""
                        update Ticket ticket
                        set ticket.createdAt = :createdAt
                        where ticket.id in :ids
                        """)
                .setParameter("createdAt", sameCreatedAt)
                .setParameter("ids", List.of(first.getId(), second.getId()))
                .executeUpdate();
        entityManager.clear();

        Pageable pageable = PageRequest.of(
                0,
                1,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<Ticket> tickets = ticketRepository.findAllBy(pageable);

        assertEquals(List.of(second.getId()), ids(tickets));
        assertEquals(2, tickets.getTotalElements());
        assertEquals("Alex Rivera", tickets.getContent().getFirst().getRequester().getFullName());
        assertEquals("Sam Lee", tickets.getContent().getFirst().getAssignedAgent().getFullName());
    }

    private AppUser saveUser(String fullName, String email, Role role) {
        return appUserRepository.saveAndFlush(new AppUser(fullName, email, role));
    }

    private Ticket saveTicket(
            String title,
            Priority priority,
            TicketStatus status,
            AppUser requester,
            AppUser assignedAgent) {
        Ticket ticket = new Ticket(title, title + " description", priority, requester);
        ticket.setStatus(status);
        ticket.setAssignedAgent(assignedAgent);
        return ticketRepository.saveAndFlush(ticket);
    }

    private List<Long> ids(List<Ticket> tickets) {
        return tickets.stream().map(Ticket::getId).toList();
    }

    private List<Long> ids(Page<Ticket> tickets) {
        return ids(tickets.getContent());
    }
}
