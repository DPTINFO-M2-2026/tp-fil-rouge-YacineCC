package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.Role;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper pour la conversion entre entités {@link Role} et DTOs correspondants.
 *
 * @see Role
 * @see RoleDTO
 * @since 1.0
 */
@ApplicationScoped
public class RoleMapper {

    /**
     * Convertit une entité {@link Role} en {@link RoleDTO}.
     */
    public RoleDTO toDTO(Role role) {
        if (role == null) return null;

        RoleDTO dto = new RoleDTO();
        dto.id = role.id;
        dto.name = role.name;
        dto.color = role.color;
        dto.guildId = role.guild != null ? role.guild.id : null;
        dto.position = role.position;
        dto.createdAt = role.createdAt;
        dto.canManageChannels = role.canManageChannels;
        dto.canManageRoles = role.canManageRoles;
        dto.canManageMessages = role.canManageMessages;
        dto.canKickMembers = role.canKickMembers;
        dto.canBanMembers = role.canBanMembers;
        dto.canSendMessages = role.canSendMessages;
        dto.canReadMessages = role.canReadMessages;

        if (role.users != null) {
            dto.userCount = role.users.size();
        }

        return dto;
    }

    /**
     * Crée une entité {@link Role} à partir d'un {@link CreateRoleDTO}.
     * Le champ {@code guild} doit être résolu par le service appelant.
     */
    public Role toEntity(CreateRoleDTO dto, Guild guild) {
        if (dto == null) return null;

        Role role = new Role();
        role.name = dto.name;
        role.color = dto.color;
        role.guild = guild;
        role.position = dto.position;
        role.canManageChannels = dto.canManageChannels;
        role.canManageRoles = dto.canManageRoles;
        role.canManageMessages = dto.canManageMessages;
        role.canKickMembers = dto.canKickMembers;
        role.canBanMembers = dto.canBanMembers;
        role.canSendMessages = dto.canSendMessages;
        role.canReadMessages = dto.canReadMessages;
        return role;
    }
}
