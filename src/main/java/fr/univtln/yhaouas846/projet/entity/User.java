package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "discord_user")
public class User extends PanacheEntity {
    
    @NotBlank
    @Size(min = 2, max = 32)
    @Column(nullable = false, length = 32)
    public String username;
    
    @NotBlank
    @Size(min = 4, max = 4)
    @Pattern(regexp = "\\d{4}")
    @Column(nullable = false, length = 4)
    public String discriminator;
    
    @Email
    @Size(max = 320)
    @Column(unique = true, length = 320)
    public String email;
    
    @Column(name = "avatar_url")
    public String avatarUrl;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "is_bot")
    public boolean isBot = false;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @JsonManagedReference("user-messages")
    public Set<Message> messages;
    
    @ManyToMany(mappedBy = "members")
    @JsonBackReference("guild-members")
    public Set<Guild> guilds;
    
    @ManyToMany(mappedBy = "users")
    @JsonBackReference("role-users")
    public Set<Role> roles;
    
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}