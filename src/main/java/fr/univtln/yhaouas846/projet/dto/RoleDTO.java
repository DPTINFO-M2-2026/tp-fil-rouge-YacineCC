package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO complet représentant un rôle Discord pour les réponses API.
 *
 * @see fr.univtln.yhaouas846.projet.entity.Role
 */
public class RoleDTO {
    
    /**
     * Identifiant unique en base de données.
     */
    public Long id;
    
    /**
     * Nom du rôle (1-100 caractères).
     */
    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
    public String name;
    
    /**
     * Couleur du rôle (format hexadécimal #RRGGBB).
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Format de couleur invalide (attendu: #RRGGBB)")
    public String color;
    
    /**
     * Identifiant de la guilde propriétaire.
     */
    @NotNull(message = "La guilde est obligatoire")
    public Long guildId;
    
    /**
     * Position d'affichage du rôle.
     */
    @Min(value = 0, message = "La position doit être >= 0")
    public Integer position;
    
    /**
     * Date de création du rôle.
     */
    public LocalDateTime createdAt;
    
    // Permissions
    public boolean canManageChannels;
    public boolean canManageRoles;
    public boolean canManageMessages;
    public boolean canKickMembers;
    public boolean canBanMembers;
    public boolean canSendMessages;
    public boolean canReadMessages;
    
    /**
     * Nombre d'utilisateurs ayant ce rôle.
     */
    public Integer userCount;
    
    /**
     * Constructeur par défaut.
     */
    public RoleDTO() {
    }
}
