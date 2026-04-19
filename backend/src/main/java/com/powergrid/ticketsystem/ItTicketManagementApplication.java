package com.powergrid.ticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================
 * IT TICKET MANAGEMENT SYSTEM - MAIN APPLICATION
 * ============================================================
 * 
 * AI-Based Centralized IT Ticket Management System for POWERGRID.
 * 
 * PHASE 1: UNIFIED TICKET INGESTION
 * - Collects tickets from Email and AI Chatbot
 * - Normalizes all tickets into a common format
 * - Stores in centralized MongoDB database
 * 
 * PHASE 8: NOTIFICATIONS & ALERTS
 * - @EnableAsync enables asynchronous email sending
 * 
 * @author POWERGRID IT Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling // Enables Spring Scheduler for email polling
@EnableAsync // Enables async email notifications (Phase 8)
public class ItTicketManagementApplication {

    /**
     * Application entry point.
     * Starts the Spring Boot application with embedded Tomcat server.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ItTicketManagementApplication.class, args);
        System.out.println("================================================");
        System.out.println("  IT TICKET MANAGEMENT SYSTEM - STARTED");
        System.out.println("  Phase 1: Unified Ticket Ingestion");
        System.out.println("  Server running on: http://localhost:8080");
        System.out.println("================================================");
    }
}
