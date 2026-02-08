package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre entités {@link Guild} et DTOs correspondants.
 *
 * @see Guild
 * @see GuildDTO
 * @since 1.0
 */
@ApplicationScoped
public class GuildMapper {

    @Inject
    UserMapper userMapper;

    /**
     * Convertit une entité {@link Guild} en {@link GuildDTO}.
     */
    public GuildDTO toDTO(Guild guild) {
        if (guild == null) return null;

        GuildDTO dto = new GuildDTO(
            guild.id,
            guild.name,
            guild.description,
            guild.iconUrl,
            guild.owner != null ? guild.owner.id : null,
            guild.discordId,
            guild.memberLimit,
            guild.createdAt
        );

        if (guild.owner != null) {
            dto.owner = userMapper.toSummaryDTO(guild.owner);
        }

        if (guild.members != null) {
            dto.memberCount = guild.members.size();
        }

        if (guild.channels != null) {
            dto.channelIds = guild.channels.stream()
                .map(c -> c.id)
                .collect(Collectors.toSet());
        }

        if (guild.roles != null) {
            dto.roleIds = guild.roles.stream()
                .map(r -> r.id)
                .collect(Collectors.toSet());
        }

        return dto;
    }

    /**
     * Convertit une entité {@link Guild} en {@link GuildSummaryDTO} (version légère).
     */
    public GuildSummaryDTO toSummaryDTO(Guild guild) {
        if (guild == null) return null;

        return new GuildSummaryDTO(
            guild.id,
            guild.name,
            guild.iconUrl,
            guild.owner != null ? guild.owner.username : null,
            guild.members != null ? guild.members.size() : 0,
            guild.createdAt
        );
    }

    /**
     * Crée une entité {@link Guild} à partir d'un {@link CreateGuildDTO}.
     * Le champ {@code owner} doit être résolu par le service appelant.
     */
    public Guild toEntity(CreateGuildDTO dto, User owner) {
        if (dto == null) return null;

        Guild guild = new Guild();
        guild.name = dto.name;
        guild.description = dto.description;
        guild.iconUrl = dto.iconUrl;
        guild.owner = owner;
        guild.discordId = dto.discordId;
        guild.memberLimit = dto.memberLimit;
        return guild;
    }

    /**
     * Met à jour une entité existante avec les champs non-null d'un {@link UpdateGuildDTO}.
     */
    public void updateEntityFromDTO(UpdateGuildDTO dto, Guild guild) {
        if (dto == null || guild == null) return;

        if (dto.name != null) guild.name = dto.name;
        if (dto.description != null) guild.description = dto.description;
        if (dto.iconUrl != null) guild.iconUrl = dto.iconUrl;
        if (dto.memberLimit != null) guild.memberLimit = dto.memberLimit;
    }
}
