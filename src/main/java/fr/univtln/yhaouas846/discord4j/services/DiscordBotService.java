package fr.univtln.yhaouas846.discord4j.services;

import fr.univtln.yhaouas846.projet.entity.*;
import fr.univtln.yhaouas846.projet.repository.ChannelRepository;
import fr.univtln.yhaouas846.projet.repository.GuildRepository;
import fr.univtln.yhaouas846.projet.repository.MessageRepository;
import fr.univtln.yhaouas846.projet.repository.RoleRepository;
import fr.univtln.yhaouas846.projet.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Service applicatif (couche métier) fournissant des opérations de haut niveau pour un bot Discord.
 *
 * <p>Ce service orchestre la création d'une guilde avec ses canaux et rôles par défaut, l'envoi
 * de messages et la gestion d'appartenance (membres/rôles). Il s'appuie sur des repositories
 * Panache pour persister les entités du domaine ({@code User}, {@code Guild}, {@code Channel},
 * {@code Role}, {@code Message}).</p>
 *
 * <h2>Transactions</h2>
 * <p>Les méthodes qui modifient l'état en base sont annotées {@link jakarta.transaction.Transactional}.
 * Les méthodes de lecture (ou purement calculatoires) peuvent rester non transactionnelles.</p>
 *
 * <h2>Permissions</h2>
 * <p>Le contrôle d'accès est volontairement simple :
 * un propriétaire de guilde est toujours autorisé ; sinon, il faut être membre et posséder un rôle
 * (dans la guilde) autorisant l'action ({@code canSendMessages}/{@code canManageMessages}).</p>
 */
@ApplicationScoped
public class DiscordBotService {

    @Inject
    UserRepository userRepository;

    @Inject
    GuildRepository guildRepository;

    @Inject
    ChannelRepository channelRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    MessageRepository messageRepository;

    /**
     * Crée une guilde et initialise les canaux/rôles par défaut.
     *
     * @param guildName nom de la guilde
     * @param ownerUsername username du propriétaire (doit exister)
     * @return guilde persistée
     * @throws IllegalArgumentException si le propriétaire est introuvable
     */
    @Transactional
    public Guild createGuildWithDefaultChannels(String guildName, String ownerUsername) {
        // Trouver ou créer l'utilisateur propriétaire
        User owner = userRepository.findByUsername(ownerUsername);
        if (owner == null) {
            throw new IllegalArgumentException("Owner user not found: " + ownerUsername);
        }

        // Créer la guilde
        Guild guild = new Guild();
        guild.name = guildName;
        guild.description = "Nouvelle guilde créée automatiquement";
        guild.owner = owner;
        guild.createdAt = LocalDateTime.now();
        if (guild.members == null) {
            guild.members = new HashSet<>();
        }
        guildRepository.persist(guild);

        // Créer les canaux par défaut
        createDefaultChannels(guild);
        
        // Créer les rôles par défaut
        createDefaultRoles(guild);

        return guild;
    }

    /**
     * Crée les canaux par défaut d'une guilde nouvellement créée.
     *
     * <p>Les canaux créés ici sont des exemples (général, annonces, vocal).</p>
     *
     * @param guild guilde cible
     */
    @Transactional
    public void createDefaultChannels(Guild guild) {
        // Canal général
        Channel general = new Channel();
        general.name = "general";
        general.description = "Canal général pour les discussions";
        general.type = Channel.ChannelType.TEXT;
        general.guild = guild;
        general.position = 0;
        channelRepository.persist(general);

        // Canal d'annonces
        Channel announcements = new Channel();
        announcements.name = "announcements";
        announcements.description = "Annonces importantes";
        announcements.type = Channel.ChannelType.TEXT;
        announcements.guild = guild;
        announcements.position = 1;
        channelRepository.persist(announcements);

        // Canal vocal
        Channel voice = new Channel();
        voice.name = "General Voice";
        voice.description = "Canal vocal général";
        voice.type = Channel.ChannelType.VOICE;
        voice.guild = guild;
        voice.position = 2;
        channelRepository.persist(voice);
    }

    /**
     * Crée les rôles par défaut d'une guilde.
     *
     * <p>Deux rôles sont créés : "Admin" (permissions étendues) et "Member" (permissions de base).</p>
     *
     * @param guild guilde cible
     */
    @Transactional
    public void createDefaultRoles(Guild guild) {
        // Rôle Admin
        Role adminRole = new Role();
        adminRole.name = "Admin";
        adminRole.color = "#FF0000";
        adminRole.guild = guild;
        adminRole.position = 3;
        adminRole.canManageChannels = true;
        adminRole.canManageRoles = true;
        adminRole.canManageMessages = true;
        adminRole.canKickMembers = true;
        adminRole.canBanMembers = true;
        roleRepository.persist(adminRole);

        // Rôle Member
        Role memberRole = new Role();
        memberRole.name = "Member";
        memberRole.color = "#0099FF";
        memberRole.guild = guild;
        memberRole.position = 1;
        memberRole.canSendMessages = true;
        memberRole.canReadMessages = true;
        roleRepository.persist(memberRole);
    }

    /**
     * Envoie un message dans un canal au nom d'un auteur.
     *
     * <p>Cette méthode vérifie l'existence des entités et applique un contrôle de permission avant
     * la persistance.</p>
     *
     * @param authorId identifiant de l'auteur
     * @param channelId identifiant du canal
     * @param content contenu du message
     * @return message persisté
     * @throws IllegalArgumentException si auteur ou canal introuvable
     * @throws SecurityException si l'auteur n'a pas la permission d'écrire
     */
    @Transactional
    public Message sendMessage(Long authorId, Long channelId, String content) {
        User author = userRepository.findById(authorId);
        Channel channel = channelRepository.findById(channelId);

        if (author == null || channel == null) {
            throw new IllegalArgumentException("Author or channel not found");
        }

        // Vérifier les permissions
        if (!canUserSendMessage(author, channel)) {
            throw new SecurityException("User doesn't have permission to send messages in this channel");
        }

        Message message = new Message();
        message.content = content;
        message.author = author;
        message.channel = channel;
        message.createdAt = LocalDateTime.now();
        messageRepository.persist(message);

        return message;
    }

    /**
     * Détermine si un utilisateur peut publier dans un canal.
     *
     * <p>Règles : propriétaire de guilde autorisé ; sinon, il faut être membre et posséder un rôle
     * dans la guilde avec {@code canSendMessages=true}.</p>
     *
     * @param user utilisateur
     * @param channel canal
     * @return {@code true} si autorisé
     */
    public boolean canUserSendMessage(User user, Channel channel) {
        // Vérifier si l'utilisateur est membre de la guilde
        Guild guild = channel.guild;
        if (guild != null && guild.owner != null && guild.owner.equals(user)) {
            return true;
        }

        Set<User> members = guild == null ? null : guild.members;
        boolean isMember = members != null && members.contains(user);
        
        if (!isMember) {
            return false;
        }

        // Vérifier les permissions des rôles
        if (user.roles == null || user.roles.isEmpty()) {
            return false;
        }
        return user.roles.stream()
                .anyMatch(role -> role != null && role.guild != null && role.guild.equals(guild) && role.canSendMessages);
    }

    /**
     * Récupère les derniers messages d'un canal (hors messages supprimés).
     *
     * @param channelId identifiant du canal
     * @param limit nombre maximum de résultats
     * @return liste de messages
     * @throws IllegalArgumentException si le canal est introuvable
     */
    public List<Message> getChannelMessages(Long channelId, int limit) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("Channel not found");
        }

        return messageRepository.findChannelMessages(channel, limit);
    }

    /**
     * Ajoute un utilisateur à une guilde et lui assigne le rôle "Member" si présent.
     *
     * @param userId identifiant de l'utilisateur
     * @param guildId identifiant de la guilde
     * @throws IllegalArgumentException si utilisateur ou guilde introuvable
     */
    @Transactional
    public void addUserToGuild(Long userId, Long guildId) {
        User user = userRepository.findById(userId);
        Guild guild = guildRepository.findById(guildId);

        if (user == null || guild == null) {
            throw new IllegalArgumentException("User or guild not found");
        }

        // Ajouter l'utilisateur à la guilde
        if (guild.members == null) {
            guild.members = new HashSet<>();
        }
        guild.members.add(user);
        
        // Assigner le rôle Member par défaut
        Role memberRole = roleRepository.findMemberRole(guild);
        if (memberRole != null) {
            if (memberRole.users == null) {
                memberRole.users = new HashSet<>();
            }
            memberRole.users.add(user);
            roleRepository.persist(memberRole);
        }

        guildRepository.persist(guild);
    }

    /**
     * Retourne les guildes d'un utilisateur (propriétaire ou membre).
     *
     * @param userId identifiant de l'utilisateur
     * @return liste de guildes
     * @throws IllegalArgumentException si l'utilisateur est introuvable
     */
    public List<Guild> getUserGuilds(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return guildRepository.findByOwnerOrMember(user);
    }

    /**
     * Détermine si un utilisateur est considéré comme "propriétaire du bot".
     *
     * <p>Implémentation volontairement simple : compare le {@code username} à une valeur fixe.</p>
     *
     * @param userId identifiant de l'utilisateur
     * @return {@code true} si l'utilisateur est considéré comme bot owner
     */
    public boolean isUserBotOwner(Long userId) {
        User user = userRepository.findById(userId);
        return user != null && "AdminUser".equals(user.username);
    }

    /**
     * Supprime logiquement un message (marque {@code isDeleted=true}) si le demandeur est autorisé.
     *
     * <p>Autorisé si le demandeur est l'auteur, ou s'il dispose d'un rôle avec
     * {@code canManageMessages=true} dans la guilde du canal.</p>
     *
     * @param messageId identifiant du message
     * @param requesterId identifiant du demandeur
     * @throws IllegalArgumentException si message ou demandeur introuvable
     * @throws SecurityException si non autorisé
     */
    @Transactional
    public void deleteMessage(Long messageId, Long requesterId) {
        Message message = messageRepository.findById(messageId);
        User requester = userRepository.findById(requesterId);

        if (message == null || requester == null) {
            throw new IllegalArgumentException("Message or requester not found");
        }

        // Vérifier si l'utilisateur peut supprimer le message
        boolean canDelete = message.author.equals(requester) || 
                           canUserManageMessages(requester, message.channel);

        if (!canDelete) {
            throw new SecurityException("User doesn't have permission to delete this message");
        }

        message.isDeleted = true;
        messageRepository.persist(message);
    }

    /**
     * Vérifie si un utilisateur peut gérer les messages dans un canal.
     *
     * <p>Le propriétaire de guilde est toujours autorisé. Sinon, l'utilisateur doit posséder un rôle
     * dans la guilde avec {@code canManageMessages=true}.</p>
     */
    private boolean canUserManageMessages(User user, Channel channel) {
        Guild guild = channel.guild;
        
        // Le propriétaire de la guilde peut toujours gérer les messages
        if (guild.owner.equals(user)) {
            return true;
        }

        // Vérifier les permissions des rôles
        if (user.roles == null || user.roles.isEmpty()) {
            return false;
        }
        return user.roles.stream()
            .anyMatch(role -> role != null && role.guild != null && role.guild.equals(guild) && role.canManageMessages);
    }
}