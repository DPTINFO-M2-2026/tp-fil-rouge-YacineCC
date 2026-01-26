package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un message publié dans un canal.
 *
 * <p>Un message est associé à un {@link #author} et à un {@link #channel}.</p>
 *
 * <h2>Suppression / édition</h2>
 * <ul>
 *   <li>{@link #isDeleted} : suppression logique (le message reste en base)</li>
 *   <li>{@link #isEdited} et {@link #updatedAt} : mis à jour automatiquement via {@link #preUpdate()}</li>
 * </ul>
 *
 * <h2>Champs optionnels</h2>
 * <p>Quelques champs permettent de représenter un embed (titre/description/couleur) et
 * un attachement.</p>
 */
@Entity
@Table(name = "message")
public class Message extends PanacheEntity {
    
    @NotBlank
    @Size(min = 1, max = 2000)
    @Column(nullable = false, length = 2000)
    public String content;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    public User author;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    public Channel channel;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "discord_id", unique = true)
    public String discordId;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @Column(name = "is_edited")
    public boolean isEdited = false;
    
    @Column(name = "is_deleted")
    public boolean isDeleted = false;
    
    @Size(max = 256)
    @Column(name = "embed_title", length = 256)
    public String embedTitle;
    
    @Size(max = 4096)
    @Column(name = "embed_description", length = 4096)
    public String embedDescription;
    
    @Column(name = "embed_color")
    public String embedColor;
    
    @Column(name = "attachment_url")
    public String attachmentUrl;

    /**
     * Initialise {@link #createdAt} lors de la première persistance.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Met à jour {@link #updatedAt} et marque le message comme édité.
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        isEdited = true;
    }
}