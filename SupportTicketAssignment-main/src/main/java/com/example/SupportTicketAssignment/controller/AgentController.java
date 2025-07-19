package com.example.SupportTicketAssignment.controller;

import com.example.SupportTicketAssignment.entity.Ticket;
import com.example.SupportTicketAssignment.entity.User;
import com.example.SupportTicketAssignment.repository.TicketRepository;
import com.example.SupportTicketAssignment.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String showAssignedTickets(Model model, Authentication auth) {
        String username = auth.getName();
        User agent = userRepository.findByUsername(username).orElse(null);
        if (agent == null || !"ROLE_AGENT".equals(agent.getRole())) {
            return "redirect:/login";
        }
        List<Ticket> assignedTickets = ticketRepository.findAll().stream()
            .filter(t -> t.getAssignedAgent() != null &&
                         t.getAssignedAgent().getId().equals(agent.getId()))
            .toList();
        model.addAttribute("tickets", assignedTickets);
        return "agent-dashboard";
    }

    @PostMapping("/ticket/{id}/update-status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!List.of("OPEN", "IN_PROGRESS", "RESOLVED").contains(status.toUpperCase())) {
            return "redirect:/agent/dashboard?error=InvalidStatus";
        }
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            ticket.setStatus(status.toUpperCase());
            if ("RESOLVED".equals(status.toUpperCase())) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
            ticketRepository.save(ticket);
        }
        return "redirect:/agent/dashboard";
    }
}
