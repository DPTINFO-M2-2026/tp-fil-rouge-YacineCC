package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.discord4j.services.DiscordBotService;
import fr.univtln.yhaouas846.projet.entity.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DiscordBotServiceTest {

    @Inject
    DiscordBotService discordBotService;

    private User testUser;
    private Guild testGuild;
    private Channel testChannel;

    @BeforeEach
    @TestTransaction
    void setUp() {
        // Nettoyage et création des données de test
        Message.deleteAll();
        Role.deleteAll();
        Channel.deleteAll();
        Guild.deleteAll();
        User.deleteAll();

        // Créer un utilisateur de test
        testUser = new User();
        testUser.username = "TestOwner";
        testUser.discriminator = "1111";
        testUser.email = "owner@test.com";
        testUser.persist();

        // Créer une guilde de test
        testGuild = new Guild();
        testGuild.name = "Test Guild Service";
        testGuild.owner = testUser;
        testGuild.members = new HashSet<>();
        testGuild.persist();

        // Créer un canal de test
        testChannel = new Channel();
        testChannel.name = "test-channel";
        testChannel.type = Channel.ChannelType.TEXT;
        testChannel.guild = testGuild;
        testChannel.persist();
    }

    @Test
    @TestTransaction
    void testCreateGuildWithDefaultChannels() {
        Guild guild = discordBotService.createGuildWithDefaultChannels("New Test Guild", "TestOwner");
        
        assertNotNull(guild);
        assertEquals("New Test Guild", guild.name);
        assertEquals(testUser.id, guild.owner.id);
        
        // Vérifier que les canaux par défaut ont été créés
        List<Channel> channels = Channel.find("guild", guild).list();
        assertTrue(channels.size() >= 3, "Should have at least 3 default channels");
        
        // Vérifier que les rôles par défaut ont été créés
        List<Role> roles = Role.find("guild", guild).list();
        assertTrue(roles.size() >= 2, "Should have at least 2 default roles");
    }

    @Test
    @TestTransaction
    void testCreateGuildWithInvalidOwner() {
        assertThrows(IllegalArgumentException.class, () -> {
            discordBotService.createGuildWithDefaultChannels("Test Guild", "NonExistentUser");
        });
    }

    @Test
    @TestTransaction
    void testSendMessage() {
        // Créer un utilisateur avec les bonnes permissions
        User sender = new User();
        sender.username = "Sender";
        sender.discriminator = "2222";
        sender.email = "sender@test.com";
        sender.roles = new HashSet<>();
        sender.persist();

        // Créer un rôle avec permissions d'envoi
        Role memberRole = new Role();
        memberRole.name = "Member";
        memberRole.guild = testGuild;
        memberRole.canSendMessages = true;
        memberRole.users = new HashSet<>();
        memberRole.persist();

        // Associer l'utilisateur au rôle et à la guilde
        memberRole.users.add(sender);
        sender.roles.add(memberRole);
        testGuild.members.add(sender);
        
        memberRole.persist();
        sender.persist();
        testGuild.persist();

        Message message = discordBotService.sendMessage(sender.id, testChannel.id, "Test message content");
        
        assertNotNull(message);
        assertEquals("Test message content", message.content);
        assertEquals(sender.id, message.author.id);
        assertEquals(testChannel.id, message.channel.id);
    }

    @Test
    @TestTransaction
    void testSendMessageWithoutPermission() {
        User unauthorizedUser = new User();
        unauthorizedUser.username = "Unauthorized";
        unauthorizedUser.discriminator = "3333";
        unauthorizedUser.email = "unauthorized@test.com";
        unauthorizedUser.roles = new HashSet<>();
        unauthorizedUser.persist();

        assertThrows(SecurityException.class, () -> {
            discordBotService.sendMessage(unauthorizedUser.id, testChannel.id, "Should fail");
        });
    }

    @Test
    @TestTransaction
    void testSendMessageWithInvalidIds() {
        assertThrows(IllegalArgumentException.class, () -> {
            discordBotService.sendMessage(999L, testChannel.id, "Test message");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            discordBotService.sendMessage(testUser.id, 999L, "Test message");
        });
    }

    @Test
    @TestTransaction
    void testAddUserToGuild() {
        User newUser = new User();
        newUser.username = "NewMember";
        newUser.discriminator = "4444";
        newUser.email = "newmember@test.com";
        newUser.persist();

        // Créer le rôle Member par défaut
        Role memberRole = new Role();
        memberRole.name = "Member";
        memberRole.guild = testGuild;
        memberRole.users = new HashSet<>();
        memberRole.persist();

        discordBotService.addUserToGuild(newUser.id, testGuild.id);

        // Vérifier que l'utilisateur a été ajouté à la guilde
        Guild updatedGuild = Guild.findById(testGuild.id);
        assertTrue(updatedGuild.members.stream().anyMatch(u -> u.id.equals(newUser.id)));
        
        // Vérifier que le rôle Member a été assigné
        Role updatedRole = Role.find("name = ?1 AND guild = ?2", "Member", testGuild).firstResult();
        assertNotNull(updatedRole);
        assertTrue(updatedRole.users.stream().anyMatch(u -> u.id.equals(newUser.id)));
    }

    @Test
    @TestTransaction
    void testGetChannelMessages() {
        // Créer quelques messages de test
        User author = new User();
        author.username = "MessageAuthor";
        author.discriminator = "5555";
        author.persist();

        for (int i = 0; i < 5; i++) {
            Message message = new Message();
            message.content = "Message " + i;
            message.author = author;
            message.channel = testChannel;
            message.isDeleted = false;
            message.persist();
        }

        List<Message> messages = discordBotService.getChannelMessages(testChannel.id, 10);
        
        assertEquals(5, messages.size());
        // Les messages devraient être triés par date de création décroissante
        assertTrue(messages.get(0).createdAt.isAfter(messages.get(4).createdAt) || 
                  messages.get(0).createdAt.isEqual(messages.get(4).createdAt));
    }

    @Test
    @TestTransaction
    void testCanUserSendMessage() {
        // Utilisateur avec permissions
        User authorizedUser = new User();
        authorizedUser.username = "Authorized";
        authorizedUser.discriminator = "6666";
        authorizedUser.roles = new HashSet<>();
        authorizedUser.persist();

        Role sendRole = new Role();
        sendRole.name = "Sender";
        sendRole.guild = testGuild;
        sendRole.canSendMessages = true;
        sendRole.users = new HashSet<>();
        sendRole.persist();

        sendRole.users.add(authorizedUser);
        authorizedUser.roles.add(sendRole);
        testGuild.members.add(authorizedUser);
        
        sendRole.persist();
        authorizedUser.persist();
        testGuild.persist();

        assertTrue(discordBotService.canUserSendMessage(authorizedUser, testChannel));

        // Utilisateur sans permissions
        User unauthorizedUser = new User();
        unauthorizedUser.username = "NoPermission";
        unauthorizedUser.discriminator = "7777";
        unauthorizedUser.roles = new HashSet<>();
        unauthorizedUser.persist();

        assertFalse(discordBotService.canUserSendMessage(unauthorizedUser, testChannel));
    }

    @Test
    @TestTransaction
    void testDeleteMessage() {
        User author = new User();
        author.username = "Author";
        author.discriminator = "8888";
        author.persist();

        Message message = new Message();
        message.content = "Message to delete";
        message.author = author;
        message.channel = testChannel;
        message.isDeleted = false;
        message.persist();

        discordBotService.deleteMessage(message.id, author.id);

        Message updatedMessage = Message.findById(message.id);
        assertTrue(updatedMessage.isDeleted);
    }

    @Test
    @TestTransaction
    void testDeleteMessageUnauthorized() {
        User author = new User();
        author.username = "Author";
        author.discriminator = "9999";
        author.persist();

        User otherUser = new User();
        otherUser.username = "OtherUser";
        otherUser.discriminator = "0000";
        otherUser.roles = new HashSet<>();
        otherUser.persist();

        Message message = new Message();
        message.content = "Message to delete";
        message.author = author;
        message.channel = testChannel;
        message.isDeleted = false;
        message.persist();

        assertThrows(SecurityException.class, () -> {
            discordBotService.deleteMessage(message.id, otherUser.id);
        });
    }

    @Test
    void testIsUserBotOwner() {
        User adminUser = new User();
        adminUser.username = "AdminUser";
        adminUser.discriminator = "0001";
        adminUser.persist();

        User regularUser = new User();
        regularUser.username = "RegularUser";
        regularUser.discriminator = "0002";
        regularUser.persist();

        assertTrue(discordBotService.isUserBotOwner(adminUser.id));
        assertFalse(discordBotService.isUserBotOwner(regularUser.id));
    }

    @Test
    @TestTransaction
    void testGetUserGuilds() {
        // Créer une autre guilde où l'utilisateur est propriétaire
        Guild ownedGuild = new Guild();
        ownedGuild.name = "Owned Guild";
        ownedGuild.owner = testUser;
        ownedGuild.members = new HashSet<>();
        ownedGuild.persist();

        // Créer une guilde où l'utilisateur est membre
        User otherOwner = new User();
        otherOwner.username = "OtherOwner";
        otherOwner.discriminator = "1234";
        otherOwner.persist();

        Guild memberGuild = new Guild();
        memberGuild.name = "Member Guild";
        memberGuild.owner = otherOwner;
        memberGuild.members = new HashSet<>();
        memberGuild.members.add(testUser);
        memberGuild.persist();

        List<Guild> userGuilds = discordBotService.getUserGuilds(testUser.id);
        
        assertTrue(userGuilds.size() >= 2);
        assertTrue(userGuilds.stream().anyMatch(g -> g.name.equals("Owned Guild")));
        assertTrue(userGuilds.stream().anyMatch(g -> g.name.equals("Member Guild")));
    }
}