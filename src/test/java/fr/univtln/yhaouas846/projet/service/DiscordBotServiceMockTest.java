package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.discord4j.services.DiscordBotService;
import fr.univtln.yhaouas846.projet.entity.*;
import fr.univtln.yhaouas846.projet.repository.ChannelRepository;
import fr.univtln.yhaouas846.projet.repository.GuildRepository;
import fr.univtln.yhaouas846.projet.repository.MessageRepository;
import fr.univtln.yhaouas846.projet.repository.RoleRepository;
import fr.univtln.yhaouas846.projet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires (purs) du DiscordBotService avec Mockito.
 * Objectif: couvrir la logique métier sans démarrer Quarkus ni accéder à la DB.
 */
@ExtendWith(MockitoExtension.class)
class DiscordBotServiceMockTest {

    @Mock
    UserRepository userRepository;

    @Mock
    GuildRepository guildRepository;

    @Mock
    ChannelRepository channelRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    private DiscordBotService discordBotService;

    @Test
    void createGuildWithDefaultChannels_persistsGuildAndDefaults() {
        User owner = new User();
        owner.id = 42L;
        owner.username = "Owner";

        when(userRepository.findByUsername("Owner")).thenReturn(owner);

        Guild guild = discordBotService.createGuildWithDefaultChannels("My Guild", "Owner");

        assertNotNull(guild);
        assertEquals("My Guild", guild.name);
        assertEquals(owner, guild.owner);

        ArgumentCaptor<Guild> guildCaptor = ArgumentCaptor.forClass(Guild.class);
        verify(guildRepository).persist(guildCaptor.capture());
        assertNotNull(guildCaptor.getValue().members);

        ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
        verify(channelRepository, times(3)).persist(channelCaptor.capture());
        List<Channel> createdChannels = channelCaptor.getAllValues();
        assertEquals(Set.of("general", "announcements", "General Voice"),
                Set.of(createdChannels.get(0).name, createdChannels.get(1).name, createdChannels.get(2).name));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(2)).persist(roleCaptor.capture());
        List<Role> createdRoles = roleCaptor.getAllValues();
        assertTrue(createdRoles.stream().anyMatch(r -> "Admin".equals(r.name) && r.canManageMessages));
        assertTrue(createdRoles.stream().anyMatch(r -> "Member".equals(r.name) && r.canSendMessages && r.canReadMessages));
    }

    @Test
    void createGuildWithDefaultChannels_throwsWhenOwnerMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> discordBotService.createGuildWithDefaultChannels("G", "missing"));

        verifyNoInteractions(guildRepository);
        verifyNoInteractions(channelRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void canUserSendMessage_ownerAlwaysAllowed() {
        User owner = new User();
        owner.id = 1L;

        Guild guild = new Guild();
        guild.owner = owner;

        Channel channel = new Channel();
        channel.guild = guild;

        assertTrue(discordBotService.canUserSendMessage(owner, channel));
    }

    @Test
    void sendMessage_persistsMessageWhenAllowed() {
        User author = new User();
        author.id = 10L;
        author.roles = new HashSet<>();

        Guild guild = new Guild();
        guild.owner = new User();
        guild.members = new HashSet<>();
        guild.members.add(author);

        Role role = new Role();
        role.guild = guild;
        role.canSendMessages = true;
        author.roles.add(role);

        Channel channel = new Channel();
        channel.id = 99L;
        channel.guild = guild;

        when(userRepository.findById(10L)).thenReturn(author);
        when(channelRepository.findById(99L)).thenReturn(channel);

        Message created = discordBotService.sendMessage(10L, 99L, "hello");

        assertNotNull(created);
        assertEquals("hello", created.content);
        assertEquals(author, created.author);
        assertEquals(channel, created.channel);
        assertNotNull(created.createdAt);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).persist(messageCaptor.capture());
        assertEquals("hello", messageCaptor.getValue().content);
    }

    @Test
    void sendMessage_throwsSecurityExceptionWhenNotAllowed() {
        User author = new User();
        author.id = 10L;
        author.roles = new HashSet<>();

        User owner = new User();
        owner.id = 1L;

        Guild guild = new Guild();
        guild.owner = owner;
        guild.members = new HashSet<>();

        Channel channel = new Channel();
        channel.id = 99L;
        channel.guild = guild;

        when(userRepository.findById(10L)).thenReturn(author);
        when(channelRepository.findById(99L)).thenReturn(channel);

        assertThrows(SecurityException.class,
                () -> discordBotService.sendMessage(10L, 99L, "nope"));
        verify(messageRepository, never()).persist((Message) any());
    }

    @Test
    void getChannelMessages_usesRepositoryQuery() {
        Channel channel = new Channel();
        channel.id = 300L;

        when(channelRepository.findById(300L)).thenReturn(channel);
        when(messageRepository.findChannelMessages(channel, 5)).thenReturn(List.of());

        List<Message> messages = discordBotService.getChannelMessages(300L, 5);
        assertNotNull(messages);
        verify(messageRepository).findChannelMessages(channel, 5);
    }

    @Test
    void deleteMessage_allowsAuthorToDelete() {
        User author = new User();
        author.id = 100L;

        Guild guild = new Guild();
        guild.owner = new User();
        guild.owner.id = 999L;

        Channel channel = new Channel();
        channel.guild = guild;

        Message message = new Message();
        message.id = 500L;
        message.author = author;
        message.channel = channel;
        message.isDeleted = false;

        when(messageRepository.findById(500L)).thenReturn(message);
        when(userRepository.findById(100L)).thenReturn(author);

        discordBotService.deleteMessage(500L, 100L);
        assertTrue(message.isDeleted);
        verify(messageRepository).persist(message);
    }

    @Test
    void deleteMessage_deniesWhenNoRights() {
        User author = new User();
        author.id = 100L;

        User requester = new User();
        requester.id = 200L;
        requester.roles = new HashSet<>();

        User owner = new User();
        owner.id = 999L;

        Guild guild = new Guild();
        guild.owner = owner;

        Channel channel = new Channel();
        channel.guild = guild;

        Message message = new Message();
        message.id = 500L;
        message.author = author;
        message.channel = channel;

        when(messageRepository.findById(500L)).thenReturn(message);
        when(userRepository.findById(200L)).thenReturn(requester);

        assertThrows(SecurityException.class, () -> discordBotService.deleteMessage(500L, 200L));
        verify(messageRepository, never()).persist((Message) any());
    }

    /**
     * Vérifie qu'un modérateur (canManageMessages) peut supprimer le message d'un autre.
     */
    @Test
    void deleteMessage_allowsModeratorWithManagePermission() {
        User author = new User();
        author.id = 100L;

        User moderator = new User();
        moderator.id = 200L;
        moderator.roles = new HashSet<>();

        User owner = new User();
        owner.id = 999L;

        Guild guild = new Guild();
        guild.owner = owner;

        Role modRole = new Role();
        modRole.guild = guild;
        modRole.canManageMessages = true;
        moderator.roles.add(modRole);

        Channel channel = new Channel();
        channel.guild = guild;

        Message message = new Message();
        message.id = 600L;
        message.author = author;
        message.channel = channel;
        message.isDeleted = false;

        when(messageRepository.findById(600L)).thenReturn(message);
        when(userRepository.findById(200L)).thenReturn(moderator);

        discordBotService.deleteMessage(600L, 200L);

        assertTrue(message.isDeleted);
        verify(messageRepository).persist(message);
    }

    /**
     * Vérifie que deleteMessage lève IllegalArgumentException si le message n'existe pas.
     */
    @Test
    void deleteMessage_throwsWhenMessageNotFound() {
        when(messageRepository.findById(9999L)).thenReturn(null);
        when(userRepository.findById(100L)).thenReturn(new User());

        assertThrows(IllegalArgumentException.class,
                () -> discordBotService.deleteMessage(9999L, 100L));
    }

    /**
     * Vérifie l'ajout d'un utilisateur à une guilde.
     */
    @Test
    void addUserToGuild_addsUserAndAssignsDefaultRole() {
        User user = new User();
        user.id = 50L;

        Guild guild = new Guild();
        guild.id = 10L;
        guild.members = new HashSet<>();

        Role memberRole = new Role();
        memberRole.name = "Member";
        memberRole.users = new HashSet<>();

        when(userRepository.findById(50L)).thenReturn(user);
        when(guildRepository.findById(10L)).thenReturn(guild);
        when(roleRepository.findMemberRole(guild)).thenReturn(memberRole);

        discordBotService.addUserToGuild(50L, 10L);

        assertTrue(guild.members.contains(user));
        assertTrue(memberRole.users.contains(user));
        verify(guildRepository).persist(guild);
        verify(roleRepository).persist(memberRole);
    }

    /**
     * Vérifie que addUserToGuild lève une exception si l'utilisateur est introuvable.
     */
    @Test
    void addUserToGuild_throwsWhenUserNotFound() {
        when(userRepository.findById(9999L)).thenReturn(null);
        when(guildRepository.findById(10L)).thenReturn(new Guild());

        assertThrows(IllegalArgumentException.class,
                () -> discordBotService.addUserToGuild(9999L, 10L));
    }

    /**
     * Vérifie que addUserToGuild lève une exception si la guilde est introuvable.
     */
    @Test
    void addUserToGuild_throwsWhenGuildNotFound() {
        when(userRepository.findById(50L)).thenReturn(new User());
        when(guildRepository.findById(9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> discordBotService.addUserToGuild(50L, 9999L));
    }
}