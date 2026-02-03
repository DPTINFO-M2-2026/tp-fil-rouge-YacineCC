package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la création d'un message.
 *
 * @see MessageDTO
 */
public class CreateMessageDTO {
    
    /**
     * Contenu du message (obligatoire).
     */
    @NotBlank(message = "Le contenu du message est obligatoire")
    @Size(min = 1, max = 2000, message = "Le contenu doit contenir entre 1 et 2000 caractères")
    public String content;
    
    /**
     * Identifiant de l'auteur (obligatoire).
     */
    @NotNull(message = "L'auteur est obligatoire")
    public Long authorId;
    
    /**
     * Identifiant du canal (obligatoire).
     */
    @NotNull(message = "Le canal est obligatoire")
    public Long channelId;
    
    /**
     * Identifiant Discord (optionnel).
     */
    public String discordId;
    
    // Champs embed optionnels
    @Size(max = 256, message = "Le titre de l'embed ne peut dépasser 256 caractères")
    public String embedTitle;
    
    @Size(max = 4096, message = "La description de l'embed ne peut dépasser 4096 caractères")
    public String embedDescription;
    
    public String embedColor;
    public String attachmentUrl;
    
    /**
     * Constructeur par défaut.
     */
    public CreateMessageDTO() {
    }
}
