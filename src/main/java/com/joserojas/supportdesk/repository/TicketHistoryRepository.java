package com.joserojas.supportdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.TicketHistory;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}
