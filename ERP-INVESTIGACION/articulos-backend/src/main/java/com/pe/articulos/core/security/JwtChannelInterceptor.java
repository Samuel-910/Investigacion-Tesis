package com.pe.articulos.core.security;

import com.pe.articulos.modules.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Autowired
    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null) {
                authorizationHeader = authorizationHeader.trim();
                String token = null;
                if (authorizationHeader.startsWith("Bearer ")) {
                    token = authorizationHeader.substring(7);
                } else if (authorizationHeader.startsWith("Bearer")) {
                    token = authorizationHeader.substring(6);
                } else {
                    token = authorizationHeader;
                }

                if (token != null && !token.isEmpty() && !jwtService.isTokenExpired(token)) {
                    String username = jwtService.extractUsername(token);
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, null);
                    accessor.setUser(authentication);
                } else {
                    System.out.println("WebSocket Token inválido o expirado");
                }
            } else {
                System.out.println("WebSocket No Authorization header present");
            }
        }

        return message;
    }
}
