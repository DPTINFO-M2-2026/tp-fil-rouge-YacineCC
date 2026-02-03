package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la mise à jour d'un utilisateur.
 *
 * <p>Ce DTO contient uniquement les champs modifiables.
 * Les champs comme createdAt, discordId ne peuvent pas être modifiés.</p>
 *
 * @see UserDTO
 */
public class UpdateUserDTO {
    
    /**
     * Nom d'utilisateur (optionnel pour mise à jour partielle).
     */
    @Size(min = 2, max = 32, message = "Le nom d'utilisateur doit contenir entre 2 et 32 caractères")
    public String username;
    
    /**
     * Discriminator (optionnel pour mise à jour partielle).
     */
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
     * Constructeur par défaut.
     */
    public UpdateUserDTO() {
    }
}
