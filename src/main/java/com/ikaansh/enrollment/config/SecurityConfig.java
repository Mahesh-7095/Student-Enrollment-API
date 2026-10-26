package com.ikaansh.enrollment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikaansh.enrollment.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic role-based access control (HTTP Basic, in-memory users – fine for an assignment, not for production).
 *   ADMIN   : everything
 *   STUDENT : read everything, create/update students, enroll/unenroll
 * Course and student deletion plus course create/update/delete are ADMIN only.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // stateless API with Basic auth, no browser session
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/h2-console/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers("/api/courses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasAnyRole("ADMIN", "STUDENT")
                .anyRequest().denyAll())
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        writeError(mapper, req.getRequestURI(), res, HttpStatus.UNAUTHORIZED, "Authentication required"))
                .accessDeniedHandler((req, res, ex) ->
                        writeError(mapper, req.getRequestURI(), res, HttpStatus.FORBIDDEN, "You do not have permission to perform this action")))
            .headers(h -> h.frameOptions(f -> f.sameOrigin())); // allow H2 console frames
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build(),
                User.withUsername("student").password(encoder.encode("student123")).roles("STUDENT").build());
    }

    private static void writeError(ObjectMapper mapper, String path, HttpServletResponse res, HttpStatus status, String msg)
            throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(),
                new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), msg, path, null));
    }
}
