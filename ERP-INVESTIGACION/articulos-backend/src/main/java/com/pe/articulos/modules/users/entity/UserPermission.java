package com.pe.articulos.modules.users.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pe.articulos.modules.permissions.entity.Permission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
@Entity
@Table(name = "user_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "permission_id" }, name = "uk_user_permission")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE user_permissions SET active = false WHERE id = ?")
@Getter 
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user") 
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference 
    private DatosPersonales user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
    @Column(name = "granted_by")
    private Long grantedBy;
    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;
    @Column(length = 500)
    private String reason; 
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; 
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    public boolean isValid() {
        if (!active)
            return false;
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }
}
