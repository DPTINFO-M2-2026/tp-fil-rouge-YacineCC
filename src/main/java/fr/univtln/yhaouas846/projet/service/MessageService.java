package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.annotation.Logged;
import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Message;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.repository.MessageRepository;
import fr.univtln.yhaouas846.projet.service.mapper.MessageMapper;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des messages.
 *
 * @see Message
 * @see MessageDTO
 * @since 1.0
 */
@ApplicationScoped
public class MessageService {

    private static final Logger LOG = Logger.getLogger(MessageService.class);

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageMapper messageMapper;

    /**
     * Liste les messages les plus récents.
     */
    public List<MessageDTO> getAllMessages(int limit) {
        LOG.debug("Récupération des messages");
        return messageRepository.find("ORDER BY createdAt DESC")
            .page(0, limit).list().stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère un message par son identifiant.
     */
    public MessageDTO getMessageById(Long id) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new ResourceNotFoundException("Message", id);
        }
        return messageMapper.toDTO(message);
    }

    /**
     * Crée un message.
     * <p>Si {@code discordId} est fourni et déjà présent, le message existant est mis à jour.</p>
     */
    @Logged
    @Transactional
    public MessageDTO createMessage(CreateMessageDTO dto) {
        User author = User.findById(dto.authorId);
        if (author == null) {
            throw new ResourceNotFoundException("Utilisateur (auteur)", dto.authorId);
        }
        Channel channel = Channel.findById(dto.channelId);
        if (channel == null) {
            throw new ResourceNotFoundException("Canal", dto.channelId);
        }

        // Upsert par discordId
        if (dto.discordId != null) {
            Message existing = messageRepository.find("discordId", dto.discordId).firstResult();
            if (existing != null) {
                existing.content = dto.content;
                existing.persist();
                return messageMapper.toDTO(existing);
            }
        }

        Message message = messageMapper.toEntity(dto, author, channel);
        message.persist();
        return messageMapper.toDTO(message);
    }

    /**
     * Met à jour un message existant.
     */
    @Logged
    @Transactional
    public MessageDTO updateMessage(Long id, CreateMessageDTO dto) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new ResourceNotFoundException("Message", id);
        }

        message.content = dto.content;
        message.embedTitle = dto.embedTitle;
        message.embedDescription = dto.embedDescription;
        message.embedColor = dto.embedColor;
        message.attachmentUrl = dto.attachmentUrl;
        message.persist();
        return messageMapper.toDTO(message);
    }

    /**
     * Supprime logiquement un message (soft-delete).
     */
    @Logged
    @Transactional
    public void deleteMessage(Long id) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new ResourceNotFoundException("Message", id);
        }
        message.isDeleted = true;
        message.persist();
    }

    /**
     * Liste les messages d'un canal (non supprimés).
     */
    public List<MessageDTO> getMessagesByChannel(Long channelId, int limit, int offset) {
        Channel channel = Channel.findById(channelId);
        if (channel == null) {
            throw new ResourceNotFoundException("Canal", channelId);
        }
        return messageRepository
            .find("channel = ?1 AND isDeleted = false ORDER BY createdAt DESC", channel)
            .page(offset, limit).list().stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Liste les messages d'un utilisateur (non supprimés).
     */
    public List<MessageDTO> getMessagesByUser(Long userId, int limit) {
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur", userId);
        }
        return messageRepository
            .find("author = ?1 AND isDeleted = false ORDER BY createdAt DESC", user)
            .page(0, limit).list().stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Recherche de messages par contenu.
     */
    public List<MessageDTO> searchMessages(String content, int limit) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }
        return messageRepository
            .find("content LIKE ?1 AND isDeleted = false ORDER BY createdAt DESC", "%" + content + "%")
            .page(0, limit).list().stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
    }
}
