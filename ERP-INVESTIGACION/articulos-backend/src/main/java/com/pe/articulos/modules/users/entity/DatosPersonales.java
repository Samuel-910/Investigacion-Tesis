package com.pe.articulos.modules.users.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.modules.compania.entity.Compania;
import com.pe.articulos.modules.compania.entity.CompaniaPersonaVinculo;
import com.pe.articulos.modules.empresa.entity.EmpresaPersonaVinculo;
import com.pe.articulos.modules.puntos.entity.Punto;
import jakarta.persistence.OneToOne;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.pe.articulos.modules.accesos.entity.AccesoMain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.EntityListeners;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datos_personales")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE datos_personales SET active = false WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@SuppressWarnings("null")
public class DatosPersonales implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(length = 200)
    private String nombre;
    @Column(name = "apemat", length = 300)
    private String apemat;
    @Column(name = "apepat", length = 300)
    private String apepat;
    @Column(name = "ver_nombre", length = 300)
    private String verNombre;
    @Column(name = "ver_apepat", length = 200)
    private String verApepat;
    @Column(name = "ver_apemat", length = 400)
    private String verApemat;
    @Column(length = 3)
    private String sexo;
    @Column(name = "nacfec")
    private LocalDate nacfec;
    @Column(length = 12)
    private String rhc;
    @Column(name = "tipodoc", length = 2)
    private String tipodoc;
    @Column(name = "numdoc", length = 15)
    private String numdoc;
    @Column(name = "fon_local", length = 25)
    private String fonLocal;
    @Column(name = "login", length = 30)
    private String login;
    @Column(name = "passwd", length = 250)
    private String passwd;
    @Column(length = 40)
    private String email;
    @Column(length = 60)
    private String foto;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_venta_actual_id")
    @ToString.Exclude
    private Punto puntoVentaActual;
    @Column(name = "ruc", length = 12)
    private String ruc;
    @Column(length = 200)
    private String direcc;
    @Column(name = "must_change_password", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean mustChangePassword = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<UserPermission> directPermissions = new HashSet<>();
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<EmpresaPersonaVinculo> vinculosEmpresas = new HashSet<>();
    @OneToOne(mappedBy = "personaBase", cascade = CascadeType.ALL)
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Compania compania;
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<CompaniaPersonaVinculo> segurosAsociados = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_id")
    @ToString.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Punto punto;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_sucursales_asignadas", joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "sucursal_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<Sucursal> sucursalesAsignadas = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_puntos_asignados", joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "punto_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<Punto> puntosAsignados = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_actual_id")
    @ToString.Exclude
    private Sucursal sucursalActual;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_modulos_asignados", 
        joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"), 
        inverseJoinColumns = @JoinColumn(name = "modulo_id")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<AccesoMain> modulosAsignados = new HashSet<>();

    @Column(name = "use_direct_permissions", nullable = false)
    @Builder.Default
    private Boolean useDirectPermissions = false;

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Set<String> getPermissionNames() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }

    public String getFullName() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return login != null ? login : String.valueOf(id);
        }
        StringBuilder sb = new StringBuilder(nombre.trim());
        if (apepat != null && !apepat.trim().isEmpty()) {
            sb.append(" ").append(apepat.trim());
        }
        if (apemat != null && !apemat.trim().isEmpty()) {
            sb.append(" ").append(apemat.trim());
        }
        return sb.toString();
    }

    @JsonProperty("nombreCompleto")
    public String getNombreCompleto() {
        return getFullName();
    }

    public String getVerifiedFullName() {
        if (verNombre != null && verApepat != null && verApemat != null) {
            return verNombre + " " + verApepat + " " + verApemat;
        }
        return getFullName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesByModulo(null);
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesByModulo(String moduloNombre) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (roles != null && !roles.isEmpty()) {
            roles.forEach(role -> {
                boolean matchesModulo = (moduloNombre == null) ||
                        (role.getAcceso() != null && moduloNombre.equalsIgnoreCase(role.getAcceso().getNombre()));
                if (matchesModulo) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                }
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        boolean permMatches = (moduloNombre == null) ||
                                (permission.getAcceso() != null
                                        && moduloNombre.equalsIgnoreCase(permission.getAcceso().getNombre()));
                        if (permMatches) {
                            authorities.add(new SimpleGrantedAuthority(permission.getName()));
                        }
                    });
                }
            });
        }
        if (useDirectPermissions != null && useDirectPermissions && directPermissions != null) {
            directPermissions.forEach(up -> {
                boolean isActive = (up.getActive() != null && up.getActive());
                boolean isNotExpired = (up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()));
                if (isActive && isNotExpired && up.getPermission() != null) {
                    boolean permMatches = (moduloNombre == null) ||
                            (up.getPermission().getAcceso() != null
                                    && moduloNombre.equalsIgnoreCase(up.getPermission().getAcceso().getNombre()));
                    if (permMatches) {
                        authorities.add(new SimpleGrantedAuthority(up.getPermission().getName()));
                    }
                }
            });
        }
        return authorities;
    }

    public Set<Permission> getEffectivePermissions() {
        Set<Permission> permissions = new HashSet<>();
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(role -> {
                permissions.addAll(getRecursivePermissions(role));
            });
        }
        if (useDirectPermissions != null && useDirectPermissions && directPermissions != null) {
            directPermissions.stream()
                    .filter(up -> up.getActive() != null && up.getActive())
                    .filter(up -> up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()))
                    .forEach(up -> {
                        if (up.getPermission() != null) {
                            permissions.add(up.getPermission());
                        }
                    });
        }
        return permissions;
    }

    private Set<Permission> getRecursivePermissions(Role role) {
        Set<Permission> perms = new HashSet<>();
        if (role.getPermissions() != null) {
            perms.addAll(role.getPermissions());
        }
        if (role.getParent() != null) {
            perms.addAll(getRecursivePermissions(role.getParent()));
        }
        return perms;
    }

    public String getPermissionMode() {
        if (useDirectPermissions != null && useDirectPermissions) {
            long directCount = countActiveDirectPermissions();
            long roleCount = roles != null ? roles.stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .distinct()
                    .count() : 0;
            return String.format("DIRECCIÓN HÍBRIDA (Rol: %d + Directos: %d)", roleCount, directCount);
        }
        return "ROL";
    }

    public Set<String> getEffectivePermissionNames() {
        return getEffectivePermissions().stream()
                .map(Permission::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasPermission(String permissionName) {
        return getEffectivePermissionNames().contains(permissionName);
    }

    public long countActiveDirectPermissions() {
        return directPermissions.stream()
                .filter(up -> up.getActive())
                .filter(up -> up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()))
                .count();
    }
}
