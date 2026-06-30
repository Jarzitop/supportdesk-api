package com.joserojas.supportdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    Page<Ticket> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    Page<Ticket> findByPriority(Priority priority, Pageable pageable);

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    Page<Ticket> findByStatusAndPriority(TicketStatus status, Priority priority, Pageable pageable);
}
