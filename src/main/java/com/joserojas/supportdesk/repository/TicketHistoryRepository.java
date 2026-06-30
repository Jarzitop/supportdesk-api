package com.joserojas.supportdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.TicketHistory;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    Page<TicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId, Pageable pageable);
}
