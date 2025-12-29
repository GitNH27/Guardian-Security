package com.GuardianSecurity.security_backend.security;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull; // Add this import for clarity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // Add this import for explicit check
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver; // ⬅️ New Import

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtSecurityTask jwtSecurityTask;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver handlerExceptionResolver; // ⬅️ New Dependency
    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Constructor with HandlerExceptionResolver
    public JwtAuthFilter(JwtSecurityTask jwtSecurityTask, UserRepository userRepository, HandlerExceptionResolver handlerExceptionResolver)
    {
        this.jwtSecurityTask = jwtSecurityTask;
        this.userRepository = userRepository;
        this.handlerExceptionResolver = handlerExceptionResolver; // Store the resolver
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
    throws ServletException, IOException
    {
        final String authHeader = request.getHeader("Authorization");
        String token = null;

        // 1. Extract token, or continue if not present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // EXIT early for public endpoints or requests without a token
        }

        // Extract token
        token = authHeader.substring(7);

        // 2. Wrap the validation logic in a try-catch and use the resolver
        try {
            // Check if user is already authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Only attempt authentication if the token is present and the context is empty
            if (token != null && authentication == null) {
                String email = jwtSecurityTask.extractUsername(token); 

                if (email != null) {
                    Optional<User> optionalUser = userRepository.findByEmail(email);
                    
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get(); 
                        
                        if (jwtSecurityTask.isTokenValid(token, user)) {
                            // Set authentication in security context
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.emptyList() // No authorities for now
                            );
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken); 
                            logger.info("Successfully authenticated user: {}", email);
                        }
                    }
                }
            }
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            logger.debug("JWT processing failed: {}", ex.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/ws-security");
    }
}