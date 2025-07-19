package com.example.SupportTicketAssignment.controller;

import com.example.SupportTicketAssignment.entity.Ticket;
import com.example.SupportTicketAssignment.entity.User;
import com.example.SupportTicketAssignment.repository.TicketRepository;
import com.example.SupportTicketAssignment.repository.UserRepository;
import com.example.SupportTicketAssignment.service.TicketAssignmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller

public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketAssignmentService ticketAssignmentService; // Inject service

    @GetMapping // Handle /tickets
    public String showTickets(Model model, Authentication auth) {
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .orElse("");
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        if (role.equals("ROLE_ADMIN")) {
            model.addAttribute("tickets", ticketRepository.findAll());
        } else if (role.equals("ROLE_AGENT")) {
            model.addAttribute("tickets", ticketRepository.findAll().stream()
                .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(user.getId()))
                .toList());
        } else {
            model.addAttribute("tickets", ticketRepository.findAll().stream()
                .filter(t -> t.getSubmittedBy().getId().equals(user.getId()))
                .toList());
        }
        return "ticket-list";
    }

    @GetMapping("/customer/ticket")
    public String showTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "ticket-form";
    }

    @PostMapping("/customer/ticket")
    public String submitTicket(@RequestParam String title, @RequestParam String description,
                               @RequestParam String category, @RequestParam String priority,
                               Authentication auth) {
        if (!List.of("LOGIN", "PAYMENT", "TECHNICAL", "OTHER").contains(category.toUpperCase())) {
            return "redirect:/customer/ticket?error=InvalidCategory";
        }
        if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority.toUpperCase())) {
            return "redirect:/customer/ticket?error=InvalidPriority";
        }
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCategory(category.toUpperCase());
        ticket.setPriority(priority.toUpperCase());
        ticket.setSubmittedBy(user);
        ticketRepository.save(ticket);
        ticketAssignmentService.assignTicket(ticket);
        return "redirect:/customer/tickets";
    }

    @GetMapping("/customer/tickets")
    public String viewMyTickets(Model model, Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("tickets", ticketRepository.findAll()
            .stream()
            .filter(t -> t.getSubmittedBy().getId().equals(user.getId()))
            .toList());
        return "ticket-list";
    }

    private User getAvailableAgent() {
        List<User> agents = userRepository.findByRoleAndAvailableTrue("ROLE_AGENT");
        if (agents.isEmpty()) return null;
        return agents.stream()
            .min(Comparator.comparingInt(agent -> (int) ticketRepository.countByAssignedAgentAndStatusIn(
                agent, List.of("OPEN", "IN_PROGRESS"))))
            .orElse(null);
    }
}