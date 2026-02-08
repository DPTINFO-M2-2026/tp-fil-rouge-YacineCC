package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.annotation.Logged;
import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.Role;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.repository.RoleRepository;
import fr.univtln.yhaouas846.projet.service.mapper.RoleMapper;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des rôles.
 *
 * @see Role
 * @see RoleDTO
 * @since 1.0
 */
@ApplicationScoped
public class RoleService {

    private static final Logger LOG = Logger.getLogger(RoleService.class);

    @Inject
    RoleRepository roleRepository;

    @Inject
    RoleMapper roleMapper;

    /**
     * Liste tous les rôles.
     */
    public List<RoleDTO> getAllRoles() {
        LOG.debug("Récupération de tous les rôles");
        return roleRepository.listAll().stream()
            .map(roleMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère un rôle par son identifiant.
     */
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle", id);
        }
        return roleMapper.toDTO(role);
    }

    /**
     * Crée un rôle.
     * <p>Si {@code discordId} est fourni et déjà présent, le rôle existant est mis à jour.</p>
     */
    @Logged
    @Transactional
    public RoleDTO createRole(CreateRoleDTO dto) {
        Guild guild = Guild.findById(dto.guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", dto.guildId);
        }

        Role role = roleMapper.toEntity(dto, guild);
        role.persist();
        return roleMapper.toDTO(role);
    }

    /**
     * Met à jour un rôle existant.
     */
    @Logged
    @Transactional
    public RoleDTO updateRole(Long id, CreateRoleDTO dto) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle", id);
        }

        Guild guild = Guild.findById(dto.guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", dto.guildId);
        }

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
        role.persist();
        return roleMapper.toDTO(role);
    }

    /**
     * Supprime un rôle.
     */
    @Logged
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle", id);
        }
        role.delete();
    }

    /**
     * Liste les rôles d'une guilde.
     */
    public List<RoleDTO> getRolesByGuild(Long guildId) {
        Guild guild = Guild.findById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", guildId);
        }
        return roleRepository.find("guild", guild).list().stream()
            .map(roleMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Assigne un rôle à un utilisateur.
     */
    @Transactional
    public void assignRoleToUser(Long roleId, Long userId) {
        Role role = roleRepository.findById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle", roleId);
        }
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur", userId);
        }

        if (role.users == null) {
            role.users = new HashSet<>();
        }
        role.users.add(user);
        role.persist();
    }

    /**
     * Retire un rôle à un utilisateur.
     */
    @Transactional
    public void removeRoleFromUser(Long roleId, Long userId) {
        Role role = roleRepository.findById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle", roleId);
        }
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur", userId);
        }

        if (role.users != null) {
            role.users.remove(user);
        }
        role.persist();
    }
}
