package com.pe.articulos.modules.roles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pe.articulos.modules.accesos.entity.AccesoMain;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString(exclude = { "permissions", "users" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE roles SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "alcance", length = 20)
    @Builder.Default
    private String alcance = "GLOBAL";

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    // RELACIÓN APAGADA LÓGICAMENTE HASTA INYECTAR LA ENTIDAD USER BRANCH ROLE
    /*
    @OneToMany(mappedBy = "role")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<com.pe.articulos.modules.users.entity.UserBranchRole> userBranchRoles = new HashSet<>();
    */

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @JsonIgnore
    private Set<DatosPersonales> users = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Role parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<Role> children = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_acceso", nullable = false)
    private AccesoMain acceso;

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
}