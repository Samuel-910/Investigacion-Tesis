package com.pe.articulos.modules.users.service.impl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final DatosPersonalesRepository datosPersonalesRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        return datosPersonalesRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.error("User not found with login: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
    }
}
