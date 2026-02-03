package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO complet représentant un message Discord pour les réponses API.
 *
 * @see fr.univtln.yhaouas846.projet.entity.Message
 */
public class MessageDTO {
    
    /**
     * Identifiant unique en base de données.
     */
    public Long id;
    
    /**
     * Contenu du message (1-2000 caractères).
     */
    @NotBlank(message = "Le contenu du message est obligatoire")
    @Size(min = 1, max = 2000, message = "Le contenu doit contenir entre 1 et 2000 caractères")
    public String content;
    
    /**
     * Identifiant de l'auteur.
     */
    @NotNull(message = "L'auteur est obligatoire")
    public Long authorId;
    
    /**
     * Informations résumées de l'auteur.
     */
    public UserSummaryDTO author;
    
    /**
     * Identifiant du canal.
     */
    @NotNull(message = "Le canal est obligatoire")
    public Long channelId;
    
    /**
     * Identifiant Discord pour synchronisation.
     */
    public String discordId;
    
    /**
     * Date de création du message.
     */
    public LocalDateTime createdAt;
    
    /**
     * Date de dernière modification.
     */
    public LocalDateTime updatedAt;
    
    /**
     * Indicateur si le message a été édité.
     */
    public boolean isEdited;
    
    /**
     * Indicateur si le message a été supprimé (logiquement).
     */
    public boolean isDeleted;
    
    // Champs embed
    public String embedTitle;
    public String embedDescription;
    public String embedColor;
    public String attachmentUrl;
    
    /**
     * Constructeur par défaut.
     */
    public MessageDTO() {
    }
}
