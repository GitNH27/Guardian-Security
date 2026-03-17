package com.GuardianSecurity.security_backend.config;

import com.GuardianSecurity.security_backend.security.JwtSecurityTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtSecurityTask jwtService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    logger.info("[STOMP] CONNECT frame received. Checking headers...");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            // CRITICAL CHECK: Ensure the bean was actually injected
                            if (jwtService == null) {
                                logger.error("[STOMP] jwtService is NULL. @Autowired failed or circular dependency detected.");
                                return message; 
                            }

                            String username = jwtService.extractUsername(token);
                            if (username != null) {
                                // Use empty authorities to satisfy .authenticated() requirement
                                UsernamePasswordAuthenticationToken auth = 
                                    new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
                                
                                accessor.setUser(auth);
                                logger.info("[STOMP] User {} authenticated via JWT", username);
                            }
                        } catch (Exception e) {
                            // This will print the exact reason for the 500 error in Azure Log Stream
                            logger.error("[STOMP] Auth Failure: ", e);
                        }
                    } else {
                        logger.warn("[STOMP] CONNECT frame missing Bearer token");
                    }
                }
                return message;
            }
        });
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
                new MessageMatcherDelegatingAuthorizationManager.Builder();
        
        // This ensures authenticated users can actually subscribe/send
        messages.anyMessage().authenticated(); 
        return messages.build();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-security")
                .setAllowedOriginPatterns("*"); // Removed SockJS for direct WebSocket connection
    }
}