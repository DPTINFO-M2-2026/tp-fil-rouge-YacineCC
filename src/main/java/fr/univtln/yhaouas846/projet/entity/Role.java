package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "role")
public class Role extends PanacheEntity {
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    public String name;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(length = 7)
    public String color = "#000000";
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "guild_id", nullable = false)
    @JsonBackReference("guild-roles")
    public Guild guild;
    
    @Min(0)
    @Column(name = "position")
    public Integer position = 0;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    // Permissions flags
    @Column(name = "can_manage_channels")
    public boolean canManageChannels = false;
    
    @Column(name = "can_manage_roles")
    public boolean canManageRoles = false;
    
    @Column(name = "can_manage_messages")
    public boolean canManageMessages = false;
    
    @Column(name = "can_kick_members")
    public boolean canKickMembers = false;
    
    @Column(name = "can_ban_members")
    public boolean canBanMembers = false;
    
    @Column(name = "can_send_messages")
    public boolean canSendMessages = true;
    
    @Column(name = "can_read_messages")
    public boolean canReadMessages = true;
    
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    public Set<User> users;
    
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}