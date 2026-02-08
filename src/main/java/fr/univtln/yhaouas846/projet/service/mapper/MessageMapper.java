package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Message;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Mapper pour la conversion entre entités {@link Message} et DTOs correspondants.
 *
 * @see Message
 * @see MessageDTO
 * @since 1.0
 */
@ApplicationScoped
public class MessageMapper {

    @Inject
    UserMapper userMapper;

    /**
     * Convertit une entité {@link Message} en {@link MessageDTO}.
     */
    public MessageDTO toDTO(Message message) {
        if (message == null) return null;

        MessageDTO dto = new MessageDTO();
        dto.id = message.id;
        dto.content = message.content;
        dto.authorId = message.author != null ? message.author.id : null;
        dto.channelId = message.channel != null ? message.channel.id : null;
        dto.discordId = message.discordId;
        dto.createdAt = message.createdAt;
        dto.updatedAt = message.updatedAt;
        dto.isEdited = message.isEdited;
        dto.isDeleted = message.isDeleted;
        dto.embedTitle = message.embedTitle;
        dto.embedDescription = message.embedDescription;
        dto.embedColor = message.embedColor;
        dto.attachmentUrl = message.attachmentUrl;

        if (message.author != null) {
            dto.author = userMapper.toSummaryDTO(message.author);
        }

        return dto;
    }

    /**
     * Crée une entité {@link Message} à partir d'un {@link CreateMessageDTO}.
     * Les champs {@code author} et {@code channel} doivent être résolus par le service.
     */
    public Message toEntity(CreateMessageDTO dto, User author, Channel channel) {
        if (dto == null) return null;

        Message message = new Message();
        message.content = dto.content;
        message.author = author;
        message.channel = channel;
        message.discordId = dto.discordId;
        message.embedTitle = dto.embedTitle;
        message.embedDescription = dto.embedDescription;
        message.embedColor = dto.embedColor;
        message.attachmentUrl = dto.attachmentUrl;
        return message;
    }
}
