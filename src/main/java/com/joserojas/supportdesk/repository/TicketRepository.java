package com.joserojas.supportdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    List<Ticket> findAllByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    List<Ticket> findByStatusOrderByCreatedAtDescIdDesc(TicketStatus status);

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    List<Ticket> findByPriorityOrderByCreatedAtDescIdDesc(Priority priority);

    @EntityGraph(attributePaths = { "requester", "assignedAgent" })
    List<Ticket> findByStatusAndPriorityOrderByCreatedAtDescIdDesc(TicketStatus status, Priority priority);
}
