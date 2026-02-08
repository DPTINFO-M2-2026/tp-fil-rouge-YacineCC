package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Guild;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper pour la conversion entre entités {@link Channel} et DTOs correspondants.
 *
 * @see Channel
 * @see ChannelDTO
 * @since 1.0
 */
@ApplicationScoped
public class ChannelMapper {

    /**
     * Convertit une entité {@link Channel} en {@link ChannelDTO}.
     */
    public ChannelDTO toDTO(Channel channel) {
        if (channel == null) return null;

        ChannelDTO dto = new ChannelDTO(
            channel.id,
            channel.name,
            channel.description,
            channel.type,
            channel.guild != null ? channel.guild.id : null,
            channel.discordId,
            channel.position,
            channel.isNsfw,
            channel.createdAt
        );

        if (channel.messages != null) {
            dto.messageCount = channel.messages.size();
        }

        return dto;
    }

    /**
     * Crée une entité {@link Channel} à partir d'un {@link CreateChannelDTO}.
     * Le champ {@code guild} doit être résolu par le service appelant.
     */
    public Channel toEntity(CreateChannelDTO dto, Guild guild) {
        if (dto == null) return null;

        Channel channel = new Channel();
        channel.name = dto.name;
        channel.description = dto.description;
        channel.type = dto.type;
        channel.guild = guild;
        channel.discordId = dto.discordId;
        channel.position = dto.position;
        channel.isNsfw = dto.isNsfw;
        return channel;
    }
}
