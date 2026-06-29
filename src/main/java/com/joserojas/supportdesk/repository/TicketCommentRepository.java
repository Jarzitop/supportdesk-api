package com.joserojas.supportdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
