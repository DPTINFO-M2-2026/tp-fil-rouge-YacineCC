package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO complet représentant un utilisateur pour les réponses API.
 *
 * <p>Ce DTO est utilisé pour exposer les informations d'un utilisateur
 * via l'API REST. Il contient tous les champs publics d'un utilisateur,
 * à l'exception des relations complexes qui sont remplacées par des identifiants.</p>
 *
 * @see fr.univtln.yhaouas846.projet.entity.User
 */
public class UserDTO {
    
    /**
     * Identifiant unique en base de données.
     */
    public Long id;
    
    /**
     * Nom d'utilisateur (2-32 caractères).
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 2, max = 32, message = "Le nom d'utilisateur doit contenir entre 2 et 32 caractères")
    public String username;
    
    /**
     * Discriminator Discord (4 chiffres).
     */
    @NotBlank(message = "Le discriminator est obligatoire")
    @Pattern(regexp = "\\d{4}", message = "Le discriminator doit être composé de 4 chiffres")
    public String discriminator;
    
    /**
     * Adresse email (optionnelle, unique).
     */
    @Email(message = "Format email invalide")
    public String email;
    
    /**
     * URL de l'avatar de l'utilisateur.
     */
    public String avatarUrl;
    
    /**
     * Identifiant Discord pour synchronisation (optionnel, unique).
     */
    public String discordId;
    
    /**
     * Indicateur si l'utilisateur est un bot.
     */
    public boolean isBot;
    
    /**
     * Date de création du compte.
     */
    public LocalDateTime createdAt;
    
    /**
     * Liste des identifiants des guildes dont l'utilisateur est membre.
     */
    public Set<Long> guildIds;
    
    /**
     * Liste des identifiants des rôles de l'utilisateur.
     */
    public Set<Long> roleIds;
    
    /**
     * Constructeur par défaut.
     */
    public UserDTO() {
    }
    
    /**
     * Constructeur complet.
     *
     * @param id identifiant unique
     * @param username nom d'utilisateur
     * @param discriminator discriminator (4 chiffres)
     * @param email adresse email
     * @param avatarUrl URL de l'avatar
     * @param discordId identifiant Discord
     * @param isBot indicateur bot
     * @param createdAt date de création
     */
    public UserDTO(Long id, String username, String discriminator, String email, 
                   String avatarUrl, String discordId, boolean isBot, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.discordId = discordId;
        this.isBot = isBot;
        this.createdAt = createdAt;
    }
}
