package com.joserojas.supportdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    Page<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId, Pageable pageable);
}
