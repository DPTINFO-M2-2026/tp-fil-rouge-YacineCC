package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entité représentant un canal (channel) au sein d'une guilde.
 *
 * <p>Un canal appartient obligatoirement à une {@link fr.univtln.yhaouas846.projet.entity.Guild}.
 * Les messages sont liés via {@link #messages}.</p>
 *
 * <h2>Champs importants</h2>
 * <ul>
 *   <li>{@code type} : type du canal (texte, vocal, etc.)</li>
 *   <li>{@code position} : ordre d'affichage (>= 0)</li>
 *   <li>{@code isNsfw} : indicateur de contenu sensible</li>
 *   <li>{@code discordId} : identifiant Discord (unique si présent)</li>
 * </ul>
 */
@Entity
@Table(name = "channel")
public class Channel extends PanacheEntity {
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    public String name;
    
    @Size(max = 1024)
    @Column(length = 1024)
    public String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ChannelType type;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "guild_id", nullable = false)
    public Guild guild;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "discord_id", unique = true)
    public String discordId;

    @Min(0)
    @Column(name = "position")
    public Integer position = 0;
    
    @Column(name = "is_nsfw")
    public boolean isNsfw = false;
    
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<Message> messages;

    /**
     * Initialise {@link #createdAt} lors de la première persistance.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public enum ChannelType {
        TEXT, VOICE, CATEGORY, NEWS, THREAD
    }
}