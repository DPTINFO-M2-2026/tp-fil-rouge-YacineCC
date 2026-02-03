package fr.univtln.yhaouas846.projet.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la création d'un rôle.
 *
 * @see RoleDTO
 */
public class CreateRoleDTO {
    
    /**
     * Nom du rôle (obligatoire).
     */
    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
    public String name;
    
    /**
     * Couleur (optionnelle, par défaut #000000).
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Format de couleur invalide (attendu: #RRGGBB)")
    public String color = "#000000";
    
    /**
     * Identifiant de la guilde (obligatoire).
     */
    @NotNull(message = "La guilde est obligatoire")
    public Long guildId;
    
    /**
     * Position (optionnelle, par défaut 0).
     */
    @Min(value = 0, message = "La position doit être >= 0")
    public Integer position = 0;
    
    // Permissions (par défaut false sauf lecture/envoi de messages)
    public boolean canManageChannels = false;
    public boolean canManageRoles = false;
    public boolean canManageMessages = false;
    public boolean canKickMembers = false;
    public boolean canBanMembers = false;
    public boolean canSendMessages = true;
    public boolean canReadMessages = true;
    
    /**
     * Constructeur par défaut.
     */
    public CreateRoleDTO() {
    }
}
