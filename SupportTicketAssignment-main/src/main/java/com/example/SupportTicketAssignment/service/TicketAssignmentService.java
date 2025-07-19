package com.example.SupportTicketAssignment.service;

import com.example.SupportTicketAssignment.entity.Ticket;
import com.example.SupportTicketAssignment.entity.User;
import com.example.SupportTicketAssignment.repository.TicketRepository;
import com.example.SupportTicketAssignment.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TicketAssignmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

   
        // Constructor injection (preferred)
    public TicketAssignmentService(UserRepository userRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }
    @Transactional
    public void assignTicket(Ticket ticket) {
            List<User> agents = userRepository.findByRoleAndAvailableTrue("ROLE_AGENT")
                .stream()
                .filter(agent -> agent.getExpertise() != null && agent.getExpertise().contains(ticket.getCategory()))
                .toList();

            if (agents.isEmpty()) {
                // Fallback: Assign to least busy agent regardless of expertise
                agents = userRepository.findByRoleAndAvailableTrue("ROLE_AGENT");
            }

            Optional<User> bestAgent = agents.stream()
                .min(Comparator.comparingInt(this::getAgentWorkload)
                    .thenComparingDouble(agent -> getAgentPerformance(agent, ticket.getCategory())));

            if (bestAgent.isPresent()) {
                ticket.setAssignedAgent(bestAgent.get());
                ticketRepository.save(ticket);
            } else {
                // Log or notify admin that no agents are available
                System.out.println("No available agents for ticket ID: " + ticket.getId());
            }
        }
 

    // Calculate workload as number of open or in-progress tickets
    private int getAgentWorkload(User agent) {
        return (int) ticketRepository.findAll().stream()
                .filter(t -> t.getAssignedAgent() != null &&
                        t.getAssignedAgent().getId().equals(agent.getId()) &&
                        List.of("OPEN", "IN_PROGRESS").contains(t.getStatus()))
                .count();
    }

    // Optional: Factor in past behavior (e.g., resolution time for similar tickets)
    public double getAgentPerformance(User agent, String category) {
        List<Ticket> pastTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getAssignedAgent() != null &&
                        t.getAssignedAgent().getId().equals(agent.getId()) &&
                        t.getCategory().equals(category) &&
                        t.getStatus().equals("RESOLVED") &&
                        t.getResolvedAt() != null)
                .toList();

        if (pastTickets.isEmpty()) {
            return Double.MAX_VALUE; // No history, lowest priority
        }

        // Average resolution time in hours
        return pastTickets.stream()
                .mapToLong(t -> java.time.Duration.between(t.getSubmittedAt(), t.getResolvedAt()).toHours())
                .average()
                .orElse(Double.MAX_VALUE);
    }
}