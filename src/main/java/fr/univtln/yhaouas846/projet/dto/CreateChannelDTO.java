package fr.univtln.yhaouas846.projet.dto;

import fr.univtln.yhaouas846.projet.entity.Channel.ChannelType;
import jakarta.validation.constraints.*;

/**
 * DTO pour la création d'un canal.
 *
 * @see ChannelDTO
 */
public class CreateChannelDTO {
    
    /**
     * Nom du canal (obligatoire).
     */
    @NotBlank(message = "Le nom du canal est obligatoire")
    @Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
    public String name;
    
    /**
     * Description (optionnelle).
     */
    @Size(max = 1024, message = "La description ne peut dépasser 1024 caractères")
    public String description;
    
    /**
     * Type du canal (obligatoire).
     */
    @NotNull(message = "Le type de canal est obligatoire")
    public ChannelType type;
    
    /**
     * Identifiant de la guilde (obligatoire).
     */
    @NotNull(message = "La guilde est obligatoire")
    public Long guildId;
    
    /**
     * Identifiant Discord (optionnel).
     */
    public String discordId;
    
    /**
     * Position (optionnelle, par défaut 0).
     */
    @Min(value = 0, message = "La position doit être >= 0")
    public Integer position = 0;
    
    /**
     * Indicateur NSFW (par défaut false).
     */
    public boolean isNsfw = false;
    
    /**
     * Constructeur par défaut.
     */
    public CreateChannelDTO() {
    }
}
