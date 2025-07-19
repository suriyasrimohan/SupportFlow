package com.example.SupportTicketAssignment.controller;


import com.example.SupportTicketAssignment.entity.Ticket;
import com.example.SupportTicketAssignment.entity.User;
import com.example.SupportTicketAssignment.repository.TicketRepository;
import com.example.SupportTicketAssignment.repository.UserRepository;
import com.example.SupportTicketAssignment.service.TicketAssignmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;
    private  PasswordEncoder passwordEncoder;
    @Autowired
    public AdminController(UserRepository userRepository, TicketRepository ticketRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/admin/dashboard")
    public String viewAllTickets(Model model) {
        List<Ticket> tickets = ticketRepository.findAll();
        List<User> agents = userRepository.findAll().stream()
                .filter(u -> "ROLE_AGENT".equals(u.getRole()))
                .toList();

        long open = ticketRepository.countByStatus("OPEN");
        long resolved = ticketRepository.countByStatus("RESOLVED");
        long inProgress = ticketRepository.countByStatus("IN_PROGRESS");

        model.addAttribute("tickets", tickets);
        model.addAttribute("agents", agents);
        model.addAttribute("openCount", open);
        model.addAttribute("resolvedCount", resolved);
        model.addAttribute("inProgressCount", inProgress);

        return "admin-dashboard";
    }
    @PostMapping("/admin/ticket/{id}/assign")
    public String assignTicket(@PathVariable Long id, @RequestParam Long agentId) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return "redirect:/admin/dashboard?error=TicketNotFound";
        }
        User agent = agentId != null ? userRepository.findById(agentId).orElse(null) : null;
        if (agentId != null && (agent == null || !"ROLE_AGENT".equals(agent.getRole()))) {
            return "redirect:/admin/dashboard?error=InvalidAgent";
        }
        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/ticket/{id}/auto-assign")
    public String autoAssignTicket(@PathVariable Long id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            TicketAssignmentService ticketAssignmentService = new TicketAssignmentService(userRepository, ticketRepository);
			ticketAssignmentService.assignTicket(ticket);
        }
        return "redirect:/admin/dashboard";
    }
    @GetMapping("/admin/agents")
    public String viewAgents(Model model) {
        List<User> agents = userRepository.findAll().stream()
                .filter(u -> "ROLE_AGENT".equals(u.getRole()))
                .toList();
        model.addAttribute("agents", agents);
        return "admin-agents";
    }

    @GetMapping("/admin/agent/{id}/edit")
    public String showEditAgentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> agentOpt = userRepository.findById(id)
                .filter(u -> "ROLE_AGENT".equals(u.getRole()));
        if (agentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Agent not found or invalid role");
            return "redirect:/admin/agents";
        }
        model.addAttribute("agent", agentOpt.get());
        return "edit-agent";
    }

    @PostMapping("/admin/agent/{id}/update")
    public String updateAgent(@PathVariable Long id, @RequestParam String expertise,
                              @RequestParam boolean available) {
        User agent = userRepository.findById(id)
                .filter(u -> "ROLE_AGENT".equals(u.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        agent.setExpertise(expertise);
        agent.setAvailable(available);
        userRepository.save(agent);
        return "redirect:/admin/agents";
    }
}
