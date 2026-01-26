package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entité représentant un utilisateur Discord (ou un utilisateur applicatif) stocké en base.
 *
 * <p>Cette entité est une {@link io.quarkus.hibernate.orm.panache.PanacheEntity} : elle hérite
 * d'un identifiant numérique ({@code id}) géré par JPA.</p>
 *
 * <h2>Contraintes</h2>
 * <ul>
 *   <li>{@code username} : obligatoire, 2..32 caractères</li>
 *   <li>{@code discriminator} : obligatoire, 4 chiffres (pattern {@code \d{4}})</li>
 *   <li>{@code email} : optionnel, unique si présent</li>
 *   <li>{@code discordId} : optionnel, unique si présent (clé de synchronisation)</li>
 * </ul>
 *
 * <h2>Relations</h2>
 * <ul>
 *   <li>{@code messages} : messages authored par l'utilisateur</li>
 *   <li>{@code guilds} : guildes dont l'utilisateur est membre</li>
 *   <li>{@code roles} : rôles attribués à l'utilisateur</li>
 * </ul>
 *
 * <p>Le champ {@code createdAt} est initialisé automatiquement lors de la persistance.</p>
 */
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
    
    @Column(name = "discord_id", unique = true)
    public String discordId;

    @Column(name = "is_bot")
    public boolean isBot = false;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Message> messages;
    
    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    public Set<Guild> guilds;
    
    @ManyToMany(mappedBy = "users")
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