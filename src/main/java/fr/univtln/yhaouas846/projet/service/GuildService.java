package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.annotation.Logged;
import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.repository.GuildRepository;
import fr.univtln.yhaouas846.projet.service.mapper.GuildMapper;
import fr.univtln.yhaouas846.projet.service.mapper.UserMapper;
import fr.univtln.yhaouas846.projet.service.exception.BusinessException;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des guildes.
 *
 * @see Guild
 * @see GuildDTO
 * @since 1.0
 */
@ApplicationScoped
public class GuildService {

    private static final Logger LOG = Logger.getLogger(GuildService.class);

    @Inject
    GuildRepository guildRepository;

    @Inject
    GuildMapper guildMapper;

    @Inject
    UserMapper userMapper;

    /**
     * Liste toutes les guildes (version résumée).
     */
    public List<GuildSummaryDTO> getAllGuilds() {
        LOG.debug("Récupération de toutes les guildes");
        return guildRepository.listAll().stream()
            .map(guildMapper::toSummaryDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère une guilde par son identifiant.
     */
    public GuildDTO getGuildById(Long id) {
        Guild guild = guildRepository.findById(id);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", id);
        }
        return guildMapper.toDTO(guild);
    }

    /**
     * Crée une guilde.
     * <p>Si {@code discordId} est fourni et déjà présent, la guilde existante est mise à jour.</p>
     */
    @Logged
    @Transactional
    public GuildDTO createGuild(CreateGuildDTO dto) {
        // Résolution du propriétaire
        User owner = User.findById(dto.ownerId);
        if (owner == null) {
            throw new ResourceNotFoundException("Utilisateur (owner)", dto.ownerId);
        }

        // Upsert par discordId
        if (dto.discordId != null) {
            Guild existing = guildRepository.find("discordId", dto.discordId).firstResult();
            if (existing != null) {
                existing.name = dto.name;
                existing.description = dto.description;
                existing.iconUrl = dto.iconUrl;
                existing.memberLimit = dto.memberLimit;
                existing.owner = owner;
                existing.persist();
                return guildMapper.toDTO(existing);
            }
        }

        Guild guild = guildMapper.toEntity(dto, owner);
        guild.persist();
        return guildMapper.toDTO(guild);
    }

    /**
     * Met à jour une guilde existante (mise à jour partielle).
     */
    @Logged
    @Transactional
    public GuildDTO updateGuild(Long id, UpdateGuildDTO dto) {
        Guild guild = guildRepository.findById(id);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", id);
        }

        guildMapper.updateEntityFromDTO(dto, guild);
        guild.persist();
        return guildMapper.toDTO(guild);
    }

    /**
     * Supprime une guilde.
     */
    @Logged
    @Transactional
    public void deleteGuild(Long id) {
        Guild guild = guildRepository.findById(id);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", id);
        }
        guild.delete();
    }

    /**
     * Ajoute un membre à une guilde.
     */
    @Transactional
    public void addMember(Long guildId, Long userId) {
        Guild guild = guildRepository.findById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", guildId);
        }
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur", userId);
        }

        if (guild.members == null) {
            guild.members = new HashSet<>();
        }
        guild.members.add(user);
        guild.persist();
    }

    /**
     * Retire un membre d'une guilde.
     */
    @Transactional
    public void removeMember(Long guildId, Long userId) {
        Guild guild = guildRepository.findById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", guildId);
        }
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Utilisateur", userId);
        }

        if (guild.members != null) {
            guild.members.remove(user);
        }
        guild.persist();
    }

    /**
     * Liste les membres d'une guilde.
     */
    public Set<UserSummaryDTO> getMembers(Long guildId) {
        Guild guild = guildRepository.findById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException("Guilde", guildId);
        }
        if (guild.members == null) {
            return Set.of();
        }
        return guild.members.stream()
            .map(userMapper::toSummaryDTO)
            .collect(Collectors.toSet());
    }
}
