package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO complet représentant une guilde (serveur Discord) pour les réponses API.
 *
 * <p>Expose toutes les informations d'une guilde y compris les références
 * aux utilisateurs, canaux et rôles via leurs identifiants.</p>
 *
 * @see fr.univtln.yhaouas846.projet.entity.Guild
 */
public class GuildDTO {
    
    /**
     * Identifiant unique en base de données.
     */
    public Long id;
    
    /**
     * Nom de la guilde (2-100 caractères).
     */
    @NotBlank(message = "Le nom de la guilde est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    public String name;
    
    /**
     * Description de la guilde (optionnelle).
     */
    @Size(max = 1024, message = "La description ne peut dépasser 1024 caractères")
    public String description;
    
    /**
     * URL de l'icône de la guilde.
     */
    public String iconUrl;
    
    /**
     * Identifiant du propriétaire de la guilde.
     */
    @NotNull(message = "Le propriétaire est obligatoire")
    public Long ownerId;
    
    /**
     * Informations résumées du propriétaire.
     */
    public UserSummaryDTO owner;
    
    /**
     * Identifiant Discord pour synchronisation.
     */
    public String discordId;
    
    /**
     * Limite de membres (2-800000).
     */
    @Min(value = 2, message = "La limite de membres doit être au moins 2")
    @Max(value = 800000, message = "La limite de membres ne peut dépasser 800000")
    public Integer memberLimit;
    
    /**
     * Date de création de la guilde.
     */
    public LocalDateTime createdAt;
    
    /**
     * Nombre de membres actuels.
     */
    public Integer memberCount;
    
    /**
     * Liste des identifiants des canaux de la guilde.
     */
    public Set<Long> channelIds;
    
    /**
     * Liste des identifiants des rôles de la guilde.
     */
    public Set<Long> roleIds;
    
    /**
     * Constructeur par défaut.
     */
    public GuildDTO() {
    }
    
    /**
     * Constructeur complet.
     *
     * @param id identifiant unique
     * @param name nom de la guilde
     * @param description description
     * @param iconUrl URL de l'icône
     * @param ownerId identifiant du propriétaire
     * @param discordId identifiant Discord
     * @param memberLimit limite de membres
     * @param createdAt date de création
     */
    public GuildDTO(Long id, String name, String description, String iconUrl,
                    Long ownerId, String discordId, Integer memberLimit, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.ownerId = ownerId;
        this.discordId = discordId;
        this.memberLimit = memberLimit;
        this.createdAt = createdAt;
    }
}
