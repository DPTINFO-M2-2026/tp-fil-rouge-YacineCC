package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre entités {@link User} et DTOs correspondants.
 *
 * <p>Ce mapper implémente le pattern Mapper pour séparer la logique de transformation
 * de la logique métier. Il utilise l'injection de dépendances Jakarta CDI
 * avec le scope {@link ApplicationScoped}.</p>
 *
 * <h2>Patterns appliqués</h2>
 * <ul>
 *   <li><b>Mapper Pattern</b> : transformation bidirectionnelle entité ↔ DTO</li>
 *   <li><b>Single Responsibility</b> : responsabilité unique de transformation</li>
 * </ul>
 *
 * @see User
 * @see UserDTO
 * @since 1.0
 */
@ApplicationScoped
public class UserMapper {
    
    /**
     * Convertit une entité {@link User} en {@link UserDTO}.
     *
     * @param user entité source
     * @return DTO correspondant, ou {@code null} si l'entité est {@code null}
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO(
            user.id,
            user.username,
            user.discriminator,
            user.email,
            user.avatarUrl,
            user.discordId,
            user.isBot,
            user.createdAt
        );
        
        // Extraction des IDs des relations
        if (user.guilds != null) {
            dto.guildIds = user.guilds.stream()
                .map(g -> g.id)
                .collect(Collectors.toSet());
        }
        
        if (user.roles != null) {
            dto.roleIds = user.roles.stream()
                .map(r -> r.id)
                .collect(Collectors.toSet());
        }
        
        return dto;
    }
    
    /**
     * Convertit une entité {@link User} en {@link UserSummaryDTO} (version légère).
     *
     * @param user entité source
     * @return DTO résumé, ou {@code null} si l'entité est {@code null}
     */
    public UserSummaryDTO toSummaryDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserSummaryDTO(
            user.id,
            user.username,
            user.discriminator,
            user.avatarUrl,
            user.isBot
        );
    }
    
    /**
     * Met à jour une entité {@link User} existante avec les données d'un {@link CreateUserDTO}.
     *
     * @param dto DTO source
     * @param user entité cible à mettre à jour
     */
    public void updateEntityFromCreateDTO(CreateUserDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        
        user.username = dto.username;
        user.discriminator = dto.discriminator;
        user.email = dto.email;
        user.avatarUrl = dto.avatarUrl;
        user.discordId = dto.discordId;
        user.isBot = dto.isBot;
    }
    
    /**
     * Met à jour une entité {@link User} existante avec les données d'un {@link UpdateUserDTO}.
     * Seuls les champs non-null du DTO sont appliqués (mise à jour partielle).
     *
     * @param dto DTO source
     * @param user entité cible à mettre à jour
     */
    public void updateEntityFromUpdateDTO(UpdateUserDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        
        if (dto.username != null) {
            user.username = dto.username;
        }
        if (dto.discriminator != null) {
            user.discriminator = dto.discriminator;
        }
        if (dto.email != null) {
            user.email = dto.email;
        }
        if (dto.avatarUrl != null) {
            user.avatarUrl = dto.avatarUrl;
        }
    }
    
    /**
     * Crée une nouvelle entité {@link User} à partir d'un {@link CreateUserDTO}.
     *
     * @param dto DTO source
     * @return nouvelle entité User (non persistée)
     */
    public User toEntity(CreateUserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        updateEntityFromCreateDTO(dto, user);
        return user;
    }
}
