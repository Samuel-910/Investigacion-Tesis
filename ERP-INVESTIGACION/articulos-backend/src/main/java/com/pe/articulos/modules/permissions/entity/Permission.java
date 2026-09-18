package com.pe.articulos.modules.permissions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

import com.pe.articulos.modules.accesos.entity.AccesoMain;

@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_module", columnList = "module"),
        @Index(name = "idx_permission_name", columnList = "name"),
        @Index(name = "idx_permission_active", columnList = "active")
})
@SQLDelete(sql = "UPDATE permissions SET active = false WHERE id = ?")
@SQLRestriction("active = true")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String module;

    @ManyToOne
    @JoinColumn(name = "id_acceso", nullable = false)
    private AccesoMain acceso;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}