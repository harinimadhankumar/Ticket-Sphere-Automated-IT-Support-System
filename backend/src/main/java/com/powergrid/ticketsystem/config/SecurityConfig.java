package com.powergrid.ticketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================
 * SECURITY CONFIGURATION
 * ============================================================
 *
 * Configuration for password encryption using BCrypt.
 * Also configures Spring Security to permit all requests since
 * authentication is handled manually by the application.
 *
 * WHAT IS BCRYPT?
 * ───────────────
 * BCrypt is a password hashing algorithm that:
 * - Uses salt internally (random data added to password before hashing)
 * - Is slow by design (takes ~100ms per password - deters brute force)
 * - Produces different hash each time (same password → different hash)
 * - Cannot be reversed (one-way function)
 *
 * SECURITY BENEFITS:
 * - Even if database is stolen, passwords cannot be recovered
 * - Each password is unique even if user uses same password everywhere
 * - Resistant to rainbow table attacks
 * - Resistant to brute force attacks (slow by design)
 *
 * @author IT Service Management Team
 * @version 1.1 - BCrypt Password Encryption + Permit All Requests
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure Spring Security to permit all requests.
     * Authentication is handled manually by the application's
     * own login endpoint, not by Spring Security filters.
     *
     * - CSRF disabled (REST API uses JSON, not form submissions)
     * - HTTP Basic disabled (removes browser popup)
     * - Form login disabled (app has its own React login page)
     * - All endpoints permitted (app manages auth internally)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }

    /**
     * Create BCryptPasswordEncoder bean for application-wide use.
     * Strength: 10 (default, balances security and performance)
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
