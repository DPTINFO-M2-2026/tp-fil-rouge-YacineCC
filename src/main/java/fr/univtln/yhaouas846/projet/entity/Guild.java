package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "guild")
public class Guild extends PanacheEntity {
    
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    public String name;
    
    @Size(max = 1024)
    @Column(length = 1024)
    public String description;
    
    @Column(name = "icon_url")
    public String iconUrl;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    public User owner;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Min(2)
    @Max(800000)
    @Column(name = "member_limit")
    public Integer memberLimit = 500000;
    
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    @JsonManagedReference("guild-channels")
    public Set<Channel> channels;
    
    @ManyToMany
    @JoinTable(
        name = "guild_members",
        joinColumns = @JoinColumn(name = "guild_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    public Set<User> members;
    
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    @JsonManagedReference("guild-roles")
    public Set<Role> roles;
    
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}