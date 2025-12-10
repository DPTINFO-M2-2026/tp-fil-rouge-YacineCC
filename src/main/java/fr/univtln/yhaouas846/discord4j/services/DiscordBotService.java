package fr.univtln.yhaouas846.discord4j.services;

import fr.univtln.yhaouas846.projet.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DiscordBotService {

    @Transactional
    public Guild createGuildWithDefaultChannels(String guildName, String ownerUsername) {
        // Trouver ou créer l'utilisateur propriétaire
        User owner = User.find("username", ownerUsername).firstResult();
        if (owner == null) {
            throw new IllegalArgumentException("Owner user not found: " + ownerUsername);
        }

        // Créer la guilde
        Guild guild = new Guild();
        guild.name = guildName;
        guild.description = "Nouvelle guilde créée automatiquement";
        guild.owner = owner;
        guild.createdAt = LocalDateTime.now();
        guild.persist();

        // Créer les canaux par défaut
        createDefaultChannels(guild);
        
        // Créer les rôles par défaut
        createDefaultRoles(guild);

        return guild;
    }

    @Transactional
    public void createDefaultChannels(Guild guild) {
        // Canal général
        Channel general = new Channel();
        general.name = "general";
        general.description = "Canal général pour les discussions";
        general.type = Channel.ChannelType.TEXT;
        general.guild = guild;
        general.position = 0;
        general.persist();

        // Canal d'annonces
        Channel announcements = new Channel();
        announcements.name = "announcements";
        announcements.description = "Annonces importantes";
        announcements.type = Channel.ChannelType.TEXT;
        announcements.guild = guild;
        announcements.position = 1;
        announcements.persist();

        // Canal vocal
        Channel voice = new Channel();
        voice.name = "General Voice";
        voice.description = "Canal vocal général";
        voice.type = Channel.ChannelType.VOICE;
        voice.guild = guild;
        voice.position = 2;
        voice.persist();
    }

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
        adminRole.persist();

        // Rôle Member
        Role memberRole = new Role();
        memberRole.name = "Member";
        memberRole.color = "#0099FF";
        memberRole.guild = guild;
        memberRole.position = 1;
        memberRole.canSendMessages = true;
        memberRole.canReadMessages = true;
        memberRole.persist();
    }

    @Transactional
    public Message sendMessage(Long authorId, Long channelId, String content) {
        User author = User.findById(authorId);
        Channel channel = Channel.findById(channelId);

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
        message.persist();

        return message;
    }

    public boolean canUserSendMessage(User user, Channel channel) {
        // Vérifier si l'utilisateur est membre de la guilde
        Guild guild = channel.guild;
        boolean isMember = guild.members.contains(user) || guild.owner.equals(user);
        
        if (!isMember) {
            return false;
        }

        // Vérifier les permissions des rôles
        return user.roles.stream()
                .anyMatch(role -> role.guild.equals(guild) && role.canSendMessages);
    }

    public List<Message> getChannelMessages(Long channelId, int limit) {
        Channel channel = Channel.findById(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("Channel not found");
        }

        return Message.find("channel = ?1 AND isDeleted = false ORDER BY createdAt DESC", channel)
                     .page(0, limit)
                     .list();
    }

    @Transactional
    public void addUserToGuild(Long userId, Long guildId) {
        User user = User.findById(userId);
        Guild guild = Guild.findById(guildId);

        if (user == null || guild == null) {
            throw new IllegalArgumentException("User or guild not found");
        }

        // Ajouter l'utilisateur à la guilde
        guild.members.add(user);
        
        // Assigner le rôle Member par défaut
        Role memberRole = Role.find("guild = ?1 AND name = ?2", guild, "Member").firstResult();
        if (memberRole != null) {
            memberRole.users.add(user);
            memberRole.persist();
        }

        guild.persist();
    }

    public List<Guild> getUserGuilds(Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return Guild.find("owner = ?1 OR ?2 MEMBER OF members", user, user).list();
    }

    public boolean isUserBotOwner(Long userId) {
        User user = User.findById(userId);
        return user != null && "AdminUser".equals(user.username);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long requesterId) {
        Message message = Message.findById(messageId);
        User requester = User.findById(requesterId);

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
        message.persist();
    }

    private boolean canUserManageMessages(User user, Channel channel) {
        Guild guild = channel.guild;
        
        // Le propriétaire de la guilde peut toujours gérer les messages
        if (guild.owner.equals(user)) {
            return true;
        }

        // Vérifier les permissions des rôles
        return user.roles.stream()
                .anyMatch(role -> role.guild.equals(guild) && role.canManageMessages);
    }
}