package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la création d'un utilisateur.
 *
 * <p>Ce DTO ne contient pas d'identifiant ni de champs auto-générés (createdAt).
 * Il est utilisé pour les requêtes POST de création d'utilisateur.</p>
 *
 * @see UserDTO
 */
public class CreateUserDTO {
    
    /**
     * Nom d'utilisateur (obligatoire, 2-32 caractères).
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 2, max = 32, message = "Le nom d'utilisateur doit contenir entre 2 et 32 caractères")
    public String username;
    
    /**
     * Discriminator Discord (obligatoire, 4 chiffres).
     */
    @NotBlank(message = "Le discriminator est obligatoire")
    @Pattern(regexp = "\\d{4}", message = "Le discriminator doit être composé de 4 chiffres")
    public String discriminator;
    
    /**
     * Adresse email (optionnelle).
     */
    @Email(message = "Format email invalide")
    public String email;
    
    /**
     * URL de l'avatar (optionnelle).
     */
    public String avatarUrl;
    
    /**
     * Identifiant Discord (optionnel).
     */
    public String discordId;
    
    /**
     * Indicateur si l'utilisateur est un bot (par défaut false).
     */
    public boolean isBot = false;
    
    /**
     * Constructeur par défaut.
     */
    public CreateUserDTO() {
    }
}
