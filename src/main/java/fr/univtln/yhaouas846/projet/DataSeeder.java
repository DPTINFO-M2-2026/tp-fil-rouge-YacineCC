package fr.univtln.yhaouas846.projet;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * DataSeeder - Initialise les données Discord Bot via API REST
 * 
 * Exécution :
 * mvn compile quarkus:dev -Dquarkus.args="seed"
 * 
 * Ou en production :
 * java -jar target/quarkus-app/quarkus-run.jar seed
 */
@QuarkusMain(name = "seeder")
public class DataSeeder implements QuarkusApplication {
    
    private static final Logger LOG = Logger.getLogger(DataSeeder.class);
    private static final String BASE_URL = "http://localhost:8080/api";
    
    private final Client client = ClientBuilder.newClient();
    
    @Override
    public int run(String... args) throws Exception {
        LOG.info("🌱 Démarrage du seeding de la base de données Discord Bot...");
        
        try {
            // Vérifier que l'API est accessible
            if (!checkApiHealth()) {
                LOG.error("❌ L'API n'est pas accessible. Assurez-vous que l'application tourne.");
                return 1;
            }
            
            // Créer les données dans l'ordre des dépendances
            LOG.info("📊 Étape 1/5 : Création des utilisateurs...");
            Long adminUserId = createAdminUser();
            Long testUserId = createTestUser();
            Long botUserId = createBotUser();
            
            LOG.info("🏰 Étape 2/5 : Création du serveur Discord...");
            Long guildId = createGuild(adminUserId);
            
            LOG.info("📺 Étape 3/5 : Création des canaux...");
            Long generalChannelId = createGeneralChannel(guildId);
            Long announcementsChannelId = createAnnouncementsChannel(guildId);
            Long voiceChannelId = createVoiceChannel(guildId);
            
            LOG.info("🎭 Étape 4/5 : Création des rôles...");
            Long adminRoleId = createAdminRole(guildId);
            Long moderatorRoleId = createModeratorRole(guildId);
            Long memberRoleId = createMemberRole(guildId);
            
            LOG.info("💬 Étape 5/5 : Création des messages...");
            createWelcomeMessage(adminUserId, generalChannelId);
            createTestMessage(testUserId, generalChannelId);
            createAnnouncementMessage(botUserId, announcementsChannelId);
            createGameMessage(testUserId, generalChannelId);
            
            LOG.info("✅ Seeding terminé avec succès !");
            LOG.info("📊 Résumé :");
            LOG.info("   - 3 utilisateurs créés");
            LOG.info("   - 1 serveur Discord créé");
            LOG.info("   - 3 canaux créés");
            LOG.info("   - 3 rôles créés");
            LOG.info("   - 4 messages créés");
            
            return 0;
            
        } catch (Exception e) {
            LOG.error("❌ Erreur lors du seeding : " + e.getMessage(), e);
            return 1;
        } finally {
            client.close();
        }
    }
    
    /**
     * Vérifie que l'API est accessible
     */
    private boolean checkApiHealth() {
        try {
            Response response = client.target(BASE_URL.replace("/api", "/q/health/ready"))
                    .request()
                    .get();
            return response.getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Crée l'utilisateur administrateur
     */
    private Long createAdminUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "AdminUser");
        user.put("discriminator", "0001");
        user.put("email", "admin@discord.test");
        user.put("avatarUrl", "https://cdn.discord.com/avatars/1/admin.png");
        user.put("isBot", false);
        
        return createEntity("/users", user);
    }
    
    /**
     * Crée l'utilisateur de test
     */
    private Long createTestUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "TestUser");
        user.put("discriminator", "0002");
        user.put("email", "user@discord.test");
        user.put("avatarUrl", "https://cdn.discord.com/avatars/2/user.png");
        user.put("isBot", false);
        
        return createEntity("/users", user);
    }
    
    /**
     * Crée le bot helper
     */
    private Long createBotUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "BotHelper");
        user.put("discriminator", "0000");
        user.put("email", "bot@discord.test");
        user.put("avatarUrl", "https://cdn.discord.com/avatars/3/bot.png");
        user.put("isBot", true);
        
        return createEntity("/users", user);
    }
    
    /**
     * Crée le serveur Discord
     */
    private Long createGuild(Long ownerId) {
        Map<String, Object> guild = new HashMap<>();
        guild.put("name", "Test Server");
        guild.put("description", "Un serveur de test pour le développement");
        guild.put("iconUrl", "https://cdn.discord.com/icons/1/server.png");
        guild.put("ownerId", ownerId);
        guild.put("memberLimit", 100);
        
        return createEntity("/guilds", guild);
    }
    
    /**
     * Crée le canal général
     */
    private Long createGeneralChannel(Long guildId) {
        Map<String, Object> channel = new HashMap<>();
        channel.put("name", "general");
        channel.put("description", "Canal général pour discussions");
        channel.put("type", "TEXT");
        channel.put("guildId", guildId);
        channel.put("position", 0);
        channel.put("isNsfw", false);
        
        return createEntity("/channels", channel);
    }
    
    /**
     * Crée le canal des annonces
     */
    private Long createAnnouncementsChannel(Long guildId) {
        Map<String, Object> channel = new HashMap<>();
        channel.put("name", "announcements");
        channel.put("description", "Annonces importantes");
        channel.put("type", "TEXT");
        channel.put("guildId", guildId);
        channel.put("position", 1);
        channel.put("isNsfw", false);
        
        return createEntity("/channels", channel);
    }
    
    /**
     * Crée le canal vocal
     */
    private Long createVoiceChannel(Long guildId) {
        Map<String, Object> channel = new HashMap<>();
        channel.put("name", "voice-general");
        channel.put("description", "Canal vocal général");
        channel.put("type", "VOICE");
        channel.put("guildId", guildId);
        channel.put("position", 2);
        channel.put("isNsfw", false);
        
        return createEntity("/channels", channel);
    }
    
    /**
     * Crée le rôle admin
     */
    private Long createAdminRole(Long guildId) {
        Map<String, Object> role = new HashMap<>();
        role.put("name", "Admin");
        role.put("color", "#FF0000");
        role.put("guildId", guildId);
        role.put("position", 3);
        role.put("canManageChannels", true);
        role.put("canManageRoles", true);
        role.put("canManageMessages", true);
        role.put("canKickMembers", true);
        role.put("canBanMembers", true);
        role.put("canSendMessages", true);
        role.put("canReadMessages", true);
        
        return createEntity("/roles", role);
    }
    
    /**
     * Crée le rôle modérateur
     */
    private Long createModeratorRole(Long guildId) {
        Map<String, Object> role = new HashMap<>();
        role.put("name", "Moderator");
        role.put("color", "#00FF00");
        role.put("guildId", guildId);
        role.put("position", 2);
        role.put("canManageChannels", true);
        role.put("canManageRoles", false);
        role.put("canManageMessages", true);
        role.put("canKickMembers", true);
        role.put("canBanMembers", false);
        role.put("canSendMessages", true);
        role.put("canReadMessages", true);
        
        return createEntity("/roles", role);
    }
    
    /**
     * Crée le rôle membre
     */
    private Long createMemberRole(Long guildId) {
        Map<String, Object> role = new HashMap<>();
        role.put("name", "Member");
        role.put("color", "#0099FF");
        role.put("guildId", guildId);
        role.put("position", 1);
        role.put("canManageChannels", false);
        role.put("canManageRoles", false);
        role.put("canManageMessages", false);
        role.put("canKickMembers", false);
        role.put("canBanMembers", false);
        role.put("canSendMessages", true);
        role.put("canReadMessages", true);
        
        return createEntity("/roles", role);
    }
    
    /**
     * Crée le message de bienvenue
     */
    private Long createWelcomeMessage(Long authorId, Long channelId) {
        Map<String, Object> message = new HashMap<>();
        message.put("content", "Bienvenue sur le serveur !");
        message.put("authorId", authorId);
        message.put("channelId", channelId);
        message.put("isEdited", false);
        message.put("isDeleted", false);
        
        return createEntity("/messages", message);
    }
    
    /**
     * Crée un message de test
     */
    private Long createTestMessage(Long authorId, Long channelId) {
        Map<String, Object> message = new HashMap<>();
        message.put("content", "Merci pour l'invitation !");
        message.put("authorId", authorId);
        message.put("channelId", channelId);
        message.put("isEdited", false);
        message.put("isDeleted", false);
        
        return createEntity("/messages", message);
    }
    
    /**
     * Crée une annonce
     */
    private Long createAnnouncementMessage(Long authorId, Long channelId) {
        Map<String, Object> message = new HashMap<>();
        message.put("content", "N'oubliez pas de lire les règles");
        message.put("authorId", authorId);
        message.put("channelId", channelId);
        message.put("isEdited", false);
        message.put("isDeleted", false);
        
        return createEntity("/messages", message);
    }
    
    /**
     * Crée un message de jeu
     */
    private Long createGameMessage(Long authorId, Long channelId) {
        Map<String, Object> message = new HashMap<>();
        message.put("content", "Quelqu'un veut jouer ?");
        message.put("authorId", authorId);
        message.put("channelId", channelId);
        message.put("isEdited", false);
        message.put("isDeleted", false);
        
        return createEntity("/messages", message);
    }
    
    /**
     * Méthode générique pour créer une entité via POST
     */
    private Long createEntity(String endpoint, Map<String, Object> data) {
        try {
            Response response = client.target(BASE_URL + endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(data));
            
            if (response.getStatus() == 201 || response.getStatus() == 200) {
                Map<String, Object> result = response.readEntity(Map.class);
                Long id = ((Number) result.get("id")).longValue();
                LOG.info("   ✅ " + endpoint + " créé : ID=" + id);
                return id;
            } else {
                String error = response.readEntity(String.class);
                LOG.error("   ❌ Erreur " + endpoint + " : " + response.getStatus() + " - " + error);
                throw new RuntimeException("Échec création " + endpoint);
            }
        } catch (Exception e) {
            LOG.error("   ❌ Exception " + endpoint + " : " + e.getMessage());
            throw new RuntimeException("Erreur création " + endpoint, e);
        }
    }
}
