package com.joserojas.supportdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.Ticket;
import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByStatusAndPriority(TicketStatus status, Priority priority);
}
