package com.pe.articulos.modules.users.service;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.users.dto.CreateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateMyProfileRequest;
import com.pe.articulos.modules.users.dto.UserResponse;
public interface UserService {
    UserResponse getMe();
    UserResponse updateMe(UpdateMyProfileRequest request);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByLogin(String login);
    
    PageResponse<UserResponse> getAllUsers(Pageable pageable);
    PageResponse<UserResponse> searchByName(String searchTerm, Pageable pageable);
    
    PageResponse<UserResponse> searchByFilter(String query, String type, Pageable pageable);
    PageResponse<UserResponse> getActiveUsers(Pageable pageable);
    PageResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable);
    PageResponse<UserResponse> getUsersByModulo(String moduloNombre, Pageable pageable);
    
    void deleteUser(Long id);
    void toggleUserStatus(Long id, boolean active);
    UserResponse assignRoles(Long userId, List<Long> roleIds);
    UserResponse removeRoles(Long userId, List<Long> roleIds);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void resetPassword(Long userId, String newPassword);
    
    UserResponse assignSucursales(Long userId, List<Long> sucursalIds);
    UserResponse removeSucursales(Long userId, List<Long> sucursalIds);
    List<com.pe.articulos.modules.sucursal.dto.SucursalDto> getSucursalesAsignadas(Long userId);

    UserResponse assignPuntos(Long userId, List<Long> puntoIds);
    UserResponse removePuntos(Long userId, List<Long> puntoIds);
    List<com.pe.articulos.modules.puntos.dto.PuntoResponseDTO> getPuntosAsignados(Long userId);

    UserResponse assignModulos(Long userId, List<Long> moduloIds);
    UserResponse assignModulosByName(Long userId, List<String> moduloNames);
    UserResponse removeModulos(Long userId, List<Long> moduloIds);
}
