package com.example.SupportTicketAssignment.repository;

import com.example.SupportTicketAssignment.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	List<User> findByRoleAndAvailableTrue(String role);
}