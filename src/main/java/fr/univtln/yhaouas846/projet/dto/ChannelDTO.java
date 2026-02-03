package fr.univtln.yhaouas846.projet.dto;

import fr.univtln.yhaouas846.projet.entity.Channel.ChannelType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO complet représentant un canal Discord pour les réponses API.
 *
 * @see fr.univtln.yhaouas846.projet.entity.Channel
 */
public class ChannelDTO {
    
    /**
     * Identifiant unique en base de données.
     */
    public Long id;
    
    /**
     * Nom du canal (1-100 caractères).
     */
    @NotBlank(message = "Le nom du canal est obligatoire")
    @Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
    public String name;
    
    /**
     * Description du canal (optionnelle).
     */
    @Size(max = 1024, message = "La description ne peut dépasser 1024 caractères")
    public String description;
    
    /**
     * Type du canal.
     */
    @NotNull(message = "Le type de canal est obligatoire")
    public ChannelType type;
    
    /**
     * Identifiant de la guilde propriétaire.
     */
    @NotNull(message = "La guilde est obligatoire")
    public Long guildId;
    
    /**
     * Identifiant Discord pour synchronisation.
     */
    public String discordId;
    
    /**
     * Position d'affichage du canal.
     */
    @Min(value = 0, message = "La position doit être >= 0")
    public Integer position;
    
    /**
     * Indicateur de contenu sensible (NSFW).
     */
    public boolean isNsfw;
    
    /**
     * Date de création du canal.
     */
    public LocalDateTime createdAt;
    
    /**
     * Nombre de messages dans le canal.
     */
    public Integer messageCount;
    
    /**
     * Constructeur par défaut.
     */
    public ChannelDTO() {
    }
    
    /**
     * Constructeur complet.
     *
     * @param id identifiant unique
     * @param name nom du canal
     * @param description description
     * @param type type de canal
     * @param guildId identifiant de la guilde
     * @param discordId identifiant Discord
     * @param position position d'affichage
     * @param isNsfw indicateur NSFW
     * @param createdAt date de création
     */
    public ChannelDTO(Long id, String name, String description, ChannelType type,
                      Long guildId, String discordId, Integer position, 
                      boolean isNsfw, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.guildId = guildId;
        this.discordId = discordId;
        this.position = position;
        this.isNsfw = isNsfw;
        this.createdAt = createdAt;
    }
}
