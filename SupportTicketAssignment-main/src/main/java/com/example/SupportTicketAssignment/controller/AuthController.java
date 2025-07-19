package com.example.SupportTicketAssignment.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.SupportTicketAssignment.entity.User;
import com.example.SupportTicketAssignment.repository.UserRepository;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam(required = false) String expertise) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error=UsernameTaken";
        }
        String normalizedRole = role.toUpperCase();
        if (!List.of("CUSTOMER", "AGENT", "ADMIN").contains(normalizedRole)) {
            return "redirect:/register?error=InvalidRole";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_" + normalizedRole);
        if ("AGENT".equals(normalizedRole) && expertise != null && !expertise.trim().isEmpty()) {
            user.setExpertise(expertise);
        }
        user.setAvailable(true);
        userRepository.save(user);
        return "redirect:/login";
    }
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
