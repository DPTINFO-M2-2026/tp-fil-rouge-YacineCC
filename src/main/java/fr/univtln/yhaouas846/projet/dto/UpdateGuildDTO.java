package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la mise à jour d'une guilde.
 *
 * <p>Tous les champs sont optionnels pour permettre des mises à jour partielles.</p>
 *
 * @see GuildDTO
 */
public class UpdateGuildDTO {
    
    /**
     * Nouveau nom de la guilde (optionnel).
     */
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    public String name;
    
    /**
     * Nouvelle description (optionnelle).
     */
    @Size(max = 1024, message = "La description ne peut dépasser 1024 caractères")
    public String description;
    
    /**
     * Nouvelle URL d'icône (optionnelle).
     */
    public String iconUrl;
    
    /**
     * Nouvelle limite de membres (optionnelle).
     */
    @Min(value = 2, message = "La limite de membres doit être au moins 2")
    @Max(value = 800000, message = "La limite de membres ne peut dépasser 800000")
    public Integer memberLimit;
    
    /**
     * Constructeur par défaut.
     */
    public UpdateGuildDTO() {
    }
}
