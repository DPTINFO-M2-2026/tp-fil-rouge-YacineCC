package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entité représentant une guilde (serveur Discord) stockée en base.
 *
 * <p>Une guilde possède un propriétaire ({@link #owner}), un ensemble de membres, des canaux
 * et des rôles. L'association {@code members} est modélisée via une table de jointure
 * {@code guild_members}.</p>
 *
 * <h2>Contraintes</h2>
 * <ul>
 *   <li>{@code name} obligatoire (2..100)</li>
 *   <li>{@code owner} obligatoire</li>
 *   <li>{@code memberLimit} borné à 2..800000 (par défaut 500000)</li>
 *   <li>{@code discordId} unique si présent (clé de synchronisation)</li>
 * </ul>
 */
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
    
    @Column(name = "discord_id", unique = true)
    public String discordId;

    @Min(2)
    @Max(800000)
    @Column(name = "member_limit")
    public Integer memberLimit = 500000;
    
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    @JsonIgnore
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
    @JsonIgnore
    public Set<Role> roles;

    /**
     * Initialise {@link #createdAt} lors de la première persistance.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}