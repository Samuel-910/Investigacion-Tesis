package com.pe.articulos.modules.users.service.impl;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ConflictException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.security.util.HttpUtils;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.users.dto.CreateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateMyProfileRequest;
import com.pe.articulos.modules.users.dto.UserResponse;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.users.service.UserService;
import com.pe.articulos.modules.accesos.entity.AccesoMain;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.mapper.PuntoMapper;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.roles.repository.RoleRepository;
import com.pe.articulos.modules.users.mapper.UserMapper;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final RoleRepository roleRepository;
    private final AccesoMainRepository accesoMainRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;
    private final com.pe.articulos.modules.sucursal.repository.SucursalRepository sucursalRepository;
    private final PuntoRepository puntoRepository;
    private final UserMapper userMapper;
    private final PuntoMapper puntoMapper;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;
    @Override
    @SuppressWarnings("null")
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());

        validateUsername(request.getUsername());
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        if (datosPersonalesRepository.existsByLogin(request.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
        }
        if (datosPersonalesRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new BadRequestException("El nombre es obligatorio");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new BadRequestException("El apellido paterno es obligatorio");
        }
        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = validateAndGetRoles(request.getRoleIds());
        } else {
            Role defaultRole = roleRepository.findByName("USUARIO")
                    .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto 'USUARIO' no encontrado"));
            roles.add(defaultRole);
        }

        DatosPersonales datosPersonales = userMapper.toEntity(request);
        datosPersonales.setPasswd(passwordEncoder.encode(request.getPassword()));
        datosPersonales.setNombre(capitalizeFirstLetter(request.getFirstName().trim()));
        datosPersonales.setApepat(capitalizeFirstLetter(request.getLastName().trim()));
        datosPersonales.setActive(true);
        datosPersonales.setRoles(roles);
        DatosPersonales savedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("User created successfully: {} (ID: {})", savedUser.getLogin(), savedUser.getId());
        logAuditAction(
                "USER_CREATE",
                "Usuario creado: " + savedUser.getLogin() + " (Email: " + savedUser.getEmail() + ") - Roles: "
                        + roles.size(),
                savedUser.getId(),
                "/api/users",
                "POST");
        return userMapper.toDto(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe() {
        String login = HttpUtils.getCurrentUsername();
        if (login == null || login.isEmpty()) {
            throw new BadRequestException("No se pudo obtener el usuario autenticado");
        }
        DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "login", login));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(UpdateMyProfileRequest request) {
        String login = HttpUtils.getCurrentUsername();
        if (login == null || login.isEmpty()) {
            throw new BadRequestException("No se pudo obtener el usuario autenticado");
        }
        DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "login", login));
        
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (datosPersonalesRepository.existsByEmailExcludingId(request.getEmail(), user.getId())) {
                throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado por otro usuario");
            }
            user.setEmail(request.getEmail());
        }

        user.setNombre(capitalizeFirstLetter(request.getFirstName().trim()));
        user.setApepat(capitalizeFirstLetter(request.getLastName().trim()));
        if (request.getApemat() != null) {
            user.setApemat(capitalizeFirstLetter(request.getApemat().trim()));
        }
        if (request.getPhone() != null) {
            user.setFonLocal(request.getPhone().trim());
        }

        DatosPersonales savedUser = datosPersonalesRepository.save(user);
        
        logAuditAction(
                "USER_UPDATE_PROFILE",
                "El usuario actualizó su propio perfil",
                savedUser.getId(),
                "/api/users/me",
                "PUT");
                
        return userMapper.toDto(savedUser);
    }

    @Override
    @SuppressWarnings("null")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            validateUsername(request.getUsername());
            if (!datosPersonales.getLogin().equals(request.getUsername()) &&
                    datosPersonalesRepository.existsByLogin(request.getUsername())) {
                throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
            }
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            validateEmail(request.getEmail());
            if (!datosPersonales.getEmail().equals(request.getEmail()) &&
                    datosPersonalesRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
            }
        }

        userMapper.updateEntityFromDto(request, datosPersonales);
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            datosPersonales.setNombre(capitalizeFirstLetter(request.getFirstName().trim()));
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            datosPersonales.setApepat(capitalizeFirstLetter(request.getLastName().trim()));
        }

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = validateAndGetRoles(request.getRoleIds());
            datosPersonales.setRoles(roles);
        }
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("User updated successfully: {} (ID: {})", updatedUser.getLogin(), updatedUser.getId());
        logAuditAction(
                "USER_UPDATE",
                "Usuario actualizado: " + updatedUser.getLogin() + " (ID: " + id + ")",
                id,
                "/api/users/" + id,
                "PUT");
        return userMapper.toDto(updatedUser);
    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return userMapper.toDto(datosPersonales);
    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByLogin(String login) {
        log.info("Getting user by login: {}", login);
        if (login == null || login.trim().isEmpty()) {
            throw new BadRequestException("El login no puede estar vacío");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "login", login));
        return userMapper.toDto(datosPersonales);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        validatePageable(pageable);
        Page<DatosPersonales> userPage = datosPersonalesRepository.findAll(pageable);
        return mapToPageResponse(userPage);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PageResponse<UserResponse> getActiveUsers(Pageable pageable) {
        log.info("Getting active users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        validatePageable(pageable);
        Page<DatosPersonales> userPage = datosPersonalesRepository.findAllActive(pageable);
        return mapToPageResponse(userPage);
    }
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        if (!datosPersonales.getActive()) {
            throw new ValidationException("El usuario ya está inactivo");
        }
        if (datosPersonales.hasRole("ADMINISTRADOR")) {
            throw new ValidationException("No se puede eliminar un usuario con rol ADMINISTRADOR");
        }
        String currentUsername = HttpUtils.getCurrentUsername();
        if (datosPersonales.getLogin().equals(currentUsername)) {
            throw new ValidationException("No puedes eliminar tu propia cuenta");
        }
        datosPersonales.setActive(false);
        datosPersonalesRepository.save(datosPersonales);
        log.info("User deleted (soft delete): {} (ID: {})", datosPersonales.getLogin(), id);
        logAuditAction(
                "USER_DELETE",
                "Usuario eliminado (soft delete): " + datosPersonales.getLogin() + " (ID: " + id + ")",
                id,
                "/api/users/" + id,
                "DELETE");
    }
    @Override
    public void toggleUserStatus(Long id, boolean active) {
        log.info("Toggling user status - id: {}, active: {}", id, active);
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        if (datosPersonales.getActive() == active) {
            String status = active ? "activo" : "inactivo";
            throw new ValidationException("El usuario ya se encuentra " + status);
        }
        if (datosPersonales.hasRole("ADMINISTRADOR") && !active) {
            throw new ValidationException("No se puede desactivar la cuenta de un ADMINISTRADOR");
        }
        String currentUsername = HttpUtils.getCurrentUsername();
        if (datosPersonales.getLogin().equals(currentUsername) && !active) {
            throw new ValidationException("No puedes desactivar tu propia cuenta");
        }
        datosPersonales.setActive(active);
        datosPersonalesRepository.save(datosPersonales);
        log.info("User status changed: {} - active: {}", datosPersonales.getLogin(), active);
        logAuditAction(
                "USER_STATUS_CHANGE",
                "Cambio de estado usuario: " + datosPersonales.getLogin() + " a " + (active ? "ACTIVO" : "INACTIVO"),
                id,
                "/api/users/" + id + "/status",
                "PATCH");
    }
    @Override
    @SuppressWarnings("null")
    public UserResponse assignRoles(Long userId, List<Long> roleIds) {
        log.info("Assigning {} roles to user: {}", roleIds.size(), userId);
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        Set<Role> rolesToAdd = validateAndGetRoles(roleIds);
        Set<Long> existingRoleIds = datosPersonales.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        Set<Long> newRoleIds = rolesToAdd.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        if (existingRoleIds.containsAll(newRoleIds)) {
            throw new ValidationException("El usuario ya tiene todos los roles especificados");
        }
        datosPersonales.getRoles().addAll(rolesToAdd);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Roles assigned successfully to user: {} (Total roles: {})",
                datosPersonales.getLogin(), updatedUser.getRoles().size());
        logAuditAction(
                "USER_ROLE_ASSIGN",
                "Roles asignados a usuario " + datosPersonales.getLogin() + ": " + roleIds,
                userId,
                "/api/users/" + userId + "/roles",
                "POST");
        return userMapper.toDto(updatedUser);
    }
    @Override
    @SuppressWarnings("null")
    public UserResponse removeRoles(Long userId, List<Long> roleIds) {
        log.info("Removing {} roles from user: {}", roleIds.size(), userId);
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol para remover");
        }
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        if (datosPersonales.getRoles().isEmpty()) {
            throw new ValidationException("El usuario no tiene roles asignados");
        }
        Set<Role> rolesToRemove = validateAndGetRoles(roleIds);
        Set<Long> userRoleIds = datosPersonales.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        Set<Long> rolesToRemoveIds = rolesToRemove.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        if (!userRoleIds.containsAll(rolesToRemoveIds)) {
            throw new ValidationException("El usuario no tiene todos los roles especificados");
        }
        datosPersonales.getRoles().removeAll(rolesToRemove);
        if (datosPersonales.getRoles().isEmpty()) {
            throw new ValidationException("Un usuario debe tener al menos un rol asignado");
        }
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Roles removed successfully from user: {} (Remaining roles: {})",
                datosPersonales.getLogin(), updatedUser.getRoles().size());
        logAuditAction(
                "USER_ROLE_REMOVE",
                "Roles removidos de usuario " + datosPersonales.getLogin() + ": " + roleIds,
                userId,
                "/api/users/" + userId + "/roles",
                "DELETE");
        return userMapper.toDto(updatedUser);
    }
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new BadRequestException("La contraseña actual es obligatoria");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }
        validatePassword(newPassword);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        if (!passwordEncoder.matches(oldPassword, datosPersonales.getPasswd())) {
            throw new ValidationException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(newPassword, datosPersonales.getPasswd())) {
            throw new ValidationException("La nueva contraseña debe ser diferente a la contraseña actual");
        }
        datosPersonales.setPasswd(passwordEncoder.encode(newPassword));
        datosPersonalesRepository.save(datosPersonales);
        log.info("Password changed successfully for user: {} (ID: {})", datosPersonales.getLogin(), userId);
        logAuditAction(
                "USER_PASSWORD_CHANGE",
                "Cambio de contraseña para usuario: " + datosPersonales.getLogin(),
                userId,
                "/api/users/" + userId + "/change-password",
                "POST");
    }
    @Override
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user: {} (Administrative)", userId);
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }
        validatePassword(newPassword);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        datosPersonales.setPasswd(passwordEncoder.encode(newPassword));
        datosPersonalesRepository.save(datosPersonales);
        log.info("Password reset successfully for user: {} (ID: {})", datosPersonales.getLogin(), userId);
        logAuditAction(
                "USER_PASSWORD_RESET",
                "Reseteo administrativo de contraseña para usuario: " + datosPersonales.getLogin(),
                userId,
                "/api/users/" + userId + "/reset-password",
                "PATCH");
    }

    @Override
    public PageResponse<UserResponse> getUsersByModulo(String moduloNombre, Pageable pageable) {
        log.info("Fetching active users for modulo: {}", moduloNombre);
        Page<DatosPersonales> users = datosPersonalesRepository.findActiveByModuloNombre(moduloNombre, pageable);
        return mapToPageResponse(users);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        log.info("Getting users by role: {} with pagination", roleName);
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }
        validatePageable(pageable);
        if (!roleRepository.existsByName(roleName)) {
            throw new ResourceNotFoundException("Rol", "nombre", roleName);
        }
        Page<DatosPersonales> results = datosPersonalesRepository.findByRoleName(roleName, pageable);
        return mapToPageResponse(results);
    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchByName(String searchTerm, Pageable pageable) {
        log.info("Searching users by term: {} with pagination", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }
        if (searchTerm.trim().length() < 2) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres");
        }
        validatePageable(pageable);
        String cleanSearchTerm = searchTerm.trim();
        if (cleanSearchTerm.matches("\\d+")) {
            var byDni = datosPersonalesRepository.findByNumdoc(cleanSearchTerm);
            if (byDni.isPresent()) {
                return new PageResponse<>(
                        List.of(userMapper.toDto(byDni.get())),
                        1, 1, pageable.getPageSize(), 0);
            }
        }
        Page<DatosPersonales> results = datosPersonalesRepository.searchByName(cleanSearchTerm, pageable);
        return mapToPageResponse(results);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PageResponse<UserResponse> searchByFilter(String query, String type, Pageable pageable) {
        Page<DatosPersonales> page;
        switch (type.toUpperCase()) {
            case "NOMBRE":
                page = datosPersonalesRepository.searchByName(query, pageable);
                break;
            case "EMAIL":
                page = datosPersonalesRepository.searchByEmail(query, pageable);
                break;
            case "ROL":
                page = datosPersonalesRepository.findActiveByRoleName(query, pageable);
                break;
            case "LOGIN":
                page = datosPersonalesRepository.searchByLogin(query, pageable);
                break;
            default: 
                page = datosPersonalesRepository.searchByAnyTerm(query, pageable);
                break;
        }
        return mapToPageResponse(page);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("El nombre de usuario es obligatorio");
        }
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            throw new BadRequestException(
                    "El nombre de usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres");
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            throw new BadRequestException(
                    "El nombre de usuario no puede tener más de " + MAX_USERNAME_LENGTH + " caracteres");
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new BadRequestException(
                    "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos");
        }
    }
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("El email es obligatorio");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new BadRequestException("El formato del email no es válido");
        }
    }
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("La contraseña es obligatoria");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new BadRequestException("La contraseña debe contener al menos una letra y un número");
        }
    }
    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Los parámetros de paginación son requeridos");
        }
        if (pageable.getPageSize() > 100) {
            throw new BadRequestException("El tamaño de página no puede ser mayor a 100");
        }
    }
    private Set<Role> validateAndGetRoles(Collection<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));
            roles.add(role);
        }
        return roles;
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    private void logAuditAction(String action, String description, Long resourceId,
            String endpoint, String method) {
        try {
            auditService.logAction(
                    "USUARIOS",
                    action,
                    description,
                    HttpUtils.getCurrentUsername(),
                    resourceId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    endpoint,
                    method);
        } catch (Exception e) {
            log.error("Error logging audit for action: {}", action, e);
        }
    }

    private PageResponse<UserResponse> mapToPageResponse(Page<DatosPersonales> page) {
        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Override
    @SuppressWarnings("null")
    public UserResponse assignSucursales(Long userId, List<Long> sucursalIds) {
        log.info("Assigning {} sucursales to user: {}", sucursalIds.size(), userId);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        Set<com.pe.articulos.modules.sucursal.entity.Sucursal> sucursalesToAdd = sucursalIds.stream()
                .map(sucursalId -> sucursalRepository.findById(sucursalId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Sucursal no encontrada con id: " + sucursalId)))
                .collect(Collectors.toSet());
        datosPersonales.getSucursalesAsignadas().addAll(sucursalesToAdd);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Sucursales assigned successfully to user: {}", datosPersonales.getLogin());
        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_SUCURSAL_ASSIGN",
                    "Sucursales asignadas a usuario " + datosPersonales.getLogin() + ": " + sucursalIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/v1/users/" + userId + "/sucursales",
                    "POST");
        } catch (Exception e) {
            log.error("Error logging audit for assign sucursales", e);
        }
        return userMapper.toDto(updatedUser);
    }
    @Override
    @SuppressWarnings("null")
    public UserResponse removeSucursales(Long userId, List<Long> sucursalIds) {
        log.info("Removing {} sucursales from user: {}", sucursalIds.size(), userId);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        Set<com.pe.articulos.modules.sucursal.entity.Sucursal> sucursalesToRemove = sucursalIds.stream()
                .map(sucursalId -> sucursalRepository.findById(sucursalId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Sucursal no encontrada con id: " + sucursalId)))
                .collect(Collectors.toSet());
        datosPersonales.getSucursalesAsignadas().removeAll(sucursalesToRemove);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Sucursales removed successfully from user: {}", datosPersonales.getLogin());
        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_SUCURSAL_REMOVE",
                    "Sucursales removidas de usuario " + datosPersonales.getLogin() + ": " + sucursalIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/v1/users/" + userId + "/sucursales",
                    "DELETE");
        } catch (Exception e) {
            log.error("Error logging audit for remove sucursales", e);
        }
        return userMapper.toDto(updatedUser);
    }
    @Override
    @SuppressWarnings("null")
    public List<com.pe.articulos.modules.sucursal.dto.SucursalDto> getSucursalesAsignadas(Long userId) {
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        return datosPersonales.getSucursalesAsignadas().stream()
                .map(sucursal -> {
                    com.pe.articulos.modules.sucursal.dto.SucursalDto dto = new com.pe.articulos.modules.sucursal.dto.SucursalDto();
                    dto.setIdSucursal(sucursal.getIdSucursal());
                    dto.setNombreSucursal(sucursal.getNombreSucursal());
                    dto.setEstado(sucursal.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse assignPuntos(Long userId, List<Long> puntoIds) {
        log.info("Assigning {} puntos to user: {}", puntoIds.size(), userId);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Set<Punto> puntosToAdd = puntoIds.stream()
                .map(puntoId -> puntoRepository.findById(puntoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Punto de venta no encontrado con id: " + puntoId)))
                .collect(Collectors.toSet());

        datosPersonales.getPuntosAsignados().addAll(puntosToAdd);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Puntos assigned successfully to user: {}", datosPersonales.getLogin());

        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_PUNTOS_ASSIGN",
                    "Puntos de venta asignados a usuario " + datosPersonales.getLogin() + ": " + puntoIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/users/" + userId + "/puntos",
                    "POST");
        } catch (Exception e) {
            log.error("Error logging audit for assign puntos", e);
        }
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse removePuntos(Long userId, List<Long> puntoIds) {
        log.info("Removing {} puntos from user: {}", puntoIds.size(), userId);
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Set<Punto> puntosToRemove = puntoIds.stream()
                .map(puntoId -> puntoRepository.findById(puntoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Punto de venta no encontrado con id: " + puntoId)))
                .collect(Collectors.toSet());

        datosPersonales.getPuntosAsignados().removeAll(puntosToRemove);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("Puntos removed successfully from user: {}", datosPersonales.getLogin());

        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_PUNTOS_REMOVE",
                    "Puntos de venta removidos de usuario " + datosPersonales.getLogin() + ": " + puntoIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/users/" + userId + "/puntos",
                    "DELETE");
        } catch (Exception e) {
            log.error("Error logging audit for remove puntos", e);
        }
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.pe.articulos.modules.puntos.dto.PuntoResponseDTO> getPuntosAsignados(Long userId) {
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        return datosPersonales.getPuntosAsignados().stream()
                .map(puntoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse assignModulos(Long userId, List<Long> moduloIds) {
        log.info("Assigning modulos {} to user {}", moduloIds, userId);
        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        
        if (moduloIds != null && !moduloIds.isEmpty()) {
            List<AccesoMain> modulosToAdd = accesoMainRepository.findAllById(moduloIds);
            for (AccesoMain modulo : modulosToAdd) {
                user.getModulosAsignados().add(modulo);
            }
        }
        
        DatosPersonales savedUser = datosPersonalesRepository.save(user);
        auditService.logAction(
                "USER",
                "ASSIGN_MODULOS",
                "Módulos asignados exitosamente al usuario con ID " + userId,
                savedUser.getUsername(),
                null,
                HttpUtils.getClientIpAddress(httpRequest),
                "Éxito",
                HttpUtils.getUserAgent(httpRequest),
                httpRequest.getRequestURI(),
                httpRequest.getMethod());
                
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponse assignModulosByName(Long userId, List<String> moduloNames) {
        log.info("Assigning modulos by name {} to user {}", moduloNames, userId);
        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        
        if (moduloNames != null && !moduloNames.isEmpty()) {
            for (String moduloName : moduloNames) {
                accesoMainRepository.findByNombre(moduloName).ifPresent(modulo -> {
                    user.getModulosAsignados().add(modulo);
                });
            }
        }
        
        DatosPersonales savedUser = datosPersonalesRepository.save(user);
        auditService.logAction(
                "USER",
                "ASSIGN_MODULOS_BY_NAME",
                "Módulos asignados exitosamente por nombre al usuario con ID " + userId,
                savedUser.getUsername(),
                null,
                HttpUtils.getClientIpAddress(httpRequest),
                "Éxito",
                HttpUtils.getUserAgent(httpRequest),
                httpRequest.getRequestURI(),
                httpRequest.getMethod());
                
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponse removeModulos(Long userId, List<Long> moduloIds) {
        log.info("Removing modulos {} from user {}", moduloIds, userId);
        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
                
        if (moduloIds != null && !moduloIds.isEmpty()) {
            user.getModulosAsignados().removeIf(modulo -> moduloIds.contains(modulo.getId()));
        }
        
        DatosPersonales savedUser = datosPersonalesRepository.save(user);
        auditService.logAction(
                "USER",
                "REMOVE_MODULOS",
                "Módulos removidos exitosamente al usuario con ID " + userId,
                savedUser.getUsername(),
                null,
                HttpUtils.getClientIpAddress(httpRequest),
                "Éxito",
                HttpUtils.getUserAgent(httpRequest),
                httpRequest.getRequestURI(),
                httpRequest.getMethod());
                
        return userMapper.toDto(savedUser);
    }
}

