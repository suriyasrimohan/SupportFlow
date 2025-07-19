# SupportTicketAssignment

A Spring Boot-based web application for managing, tracking, and resolving support tickets with role-based access for Customers, Agents, and Admins.

## Overview

SupportTicketAssignment is a web application designed to streamline support ticket management. It allows:

- **Customers** to submit and track tickets  
- **Agents** to resolve tickets based on their expertise  
- **Admins** to manage users and oversee ticket activity  

The application features a modern, responsive UI with role-based authentication and authorization.


## Features

- **User Registration and Login**  
  Users can register as:
  - **Customer**
  - **Agent** (must specify their expertise like login, payment, etc.)
  - **Admin**

- **Role-Based Access:**
  - **Customers:** Create and view their tickets.
  - **Agents:** View and resolve tickets matching their expertise.
  - **Admins:** Manage users and oversee all tickets.

- **Ticket Management:**  
  Create, assign, update, and resolve support tickets through the web interface.


## Technologies Used

- **Backend:** Spring Boot, Spring Security, Spring Data JPA  
- **Frontend:** Thymeleaf, HTML, CSS  
- **Database:** H2 (in-memory) or MySQL  
- **Build Tool:** Maven  
- **Java Version:** 17 or later  


## Access the Application

Once the application is running, open:  
[http://localhost:8080/home](http://localhost:8080/home)

## Project Structure
```
SupportTicketAssignment/
├── src/
│ ├── main/
│ │ ├── java/com/example/supportticketsystem/
│ │ │ ├── controller/ # Controllers (web layer)
│ │ │ ├── model/ # Entity classes
│ │ │ ├── repository/ # JPA repositories
│ │ │ ├── service/ # Business logic
│ │ │ ├── config/ # Security and application configs
│ │ ├── resources/
│ │ │ ├── static/css/ # CSS files
│ │ │ ├── templates/ # Thymeleaf templates
│ │ │ └── application.properties # Application configuration
│ ├── test/ # Unit and integration tests
├── pom.xml # Maven configuration
├── README.md # Project documentation
```
