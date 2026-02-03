package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la création d'une guilde.
 *
 * <p>Utilisé lors des requêtes POST. Ne contient pas d'identifiant
 * ni de champs auto-générés.</p>
 *
 * @see GuildDTO
 */
public class CreateGuildDTO {
    
    /**
     * Nom de la guilde (obligatoire, 2-100 caractères).
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
     * URL de l'icône (optionnelle).
     */
    public String iconUrl;
    
    /**
     * Identifiant du propriétaire (obligatoire).
     */
    @NotNull(message = "Le propriétaire est obligatoire")
    public Long ownerId;
    
    /**
     * Identifiant Discord (optionnel).
     */
    public String discordId;
    
    /**
     * Limite de membres (optionnelle, par défaut 500000).
     */
    @Min(value = 2, message = "La limite de membres doit être au moins 2")
    @Max(value = 800000, message = "La limite de membres ne peut dépasser 800000")
    public Integer memberLimit = 500000;
    
    /**
     * Constructeur par défaut.
     */
    public CreateGuildDTO() {
    }
}
