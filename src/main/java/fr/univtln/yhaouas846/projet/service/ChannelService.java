package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.annotation.Logged;
import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.repository.ChannelRepository;
import fr.univtln.yhaouas846.projet.service.mapper.ChannelMapper;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des canaux.
 *
 * @see Channel
 * @see ChannelDTO
 * @since 1.0
 */
@ApplicationScoped
public class ChannelService {

    private static final Logger LOG = Logger.getLogger(ChannelService.class);

    @Inject
    ChannelRepository channelRepository;

    @Inject
    ChannelMapper channelMapper;

    /**
     * Liste tous les canaux.
     */
    public List<ChannelDTO> getAllChannels() {
        LOG.debug("Récupération de tous les canaux");
        return channelRepository.listAll().stream()
            .map(channelMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère un canal par son identifiant.
     */
    public ChannelDTO getChannelById(Long id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new ResourceNotFoundException("Canal", id);
        }
        return channelMapper.toDTO(channel);
    }

    /**
     * Crée un canal.
     * <p>Si {@code discordId} est fourni et déjà présent, le canal existant est mis à jour.</p>
     */
    @Logged
    @Transactional
    public ChannelDTO createChannel(CreateChannelDTO dto) {
        Guild guild = Guild.findById(dto.guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", dto.guildId);
        }

        // Upsert par discordId
        if (dto.discordId != null) {
            Channel existing = channelRepository.find("discordId", dto.discordId).firstResult();
            if (existing != null) {
                existing.name = dto.name;
                existing.description = dto.description;
                existing.type = dto.type;
                existing.position = dto.position;
                existing.isNsfw = dto.isNsfw;
                existing.guild = guild;
                existing.persist();
                return channelMapper.toDTO(existing);
            }
        }

        Channel channel = channelMapper.toEntity(dto, guild);
        channel.persist();
        return channelMapper.toDTO(channel);
    }

    /**
     * Met à jour un canal existant.
     */
    @Logged
    @Transactional
    public ChannelDTO updateChannel(Long id, CreateChannelDTO dto) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new ResourceNotFoundException("Canal", id);
        }

        Guild guild = Guild.findById(dto.guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", dto.guildId);
        }

        channel.name = dto.name;
        channel.description = dto.description;
        channel.type = dto.type;
        channel.position = dto.position;
        channel.isNsfw = dto.isNsfw;
        channel.guild = guild;
        channel.persist();
        return channelMapper.toDTO(channel);
    }

    /**
     * Supprime un canal.
     */
    @Logged
    @Transactional
    public void deleteChannel(Long id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new ResourceNotFoundException("Canal", id);
        }
        channel.delete();
    }

    /**
     * Liste les canaux d'une guilde.
     */
    public List<ChannelDTO> getChannelsByGuild(Long guildId) {
        Guild guild = Guild.findById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", guildId);
        }
        return channelRepository.find("guild", guild).list().stream()
            .map(channelMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Liste les canaux par type.
     */
    public List<ChannelDTO> getChannelsByType(Channel.ChannelType type) {
        return channelRepository.find("type", type).list().stream()
            .map(channelMapper::toDTO)
            .collect(Collectors.toList());
    }
}
