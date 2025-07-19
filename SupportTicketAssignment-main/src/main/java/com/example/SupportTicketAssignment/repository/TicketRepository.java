package com.example.SupportTicketAssignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SupportTicketAssignment.entity.Ticket;
import com.example.SupportTicketAssignment.entity.User;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	long countByStatus(String status);
	long countByAssignedAgentAndStatusIn(User agent, List<String> statuses);
}
