package fr.univtln.yhaouas846.projet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Simple Data Seeder utilisant uniquement HTTP (sans dépendances Quarkus).
 * Envoie des requêtes POST aux endpoints REST pour peupler la base de données.
 */
public class SimpleDataSeeder {
    
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   Discord Bot Data Seeder (Simple HTTP)  ");
        System.out.println("===========================================\n");
        
        try {
            // 1. Créer les utilisateurs
            System.out.println("📝 Création des utilisateurs...");
            long adminId = createUser("admin", "admin@example.com", "admin-avatar.png");
            long user1Id = createUser("alice", "alice@example.com", "alice-avatar.png");
            long user2Id = createUser("bob", "bob@example.com", "bob-avatar.png");
            System.out.println("✅ 3 utilisateurs créés\n");
            
            // 2. Créer la guilde
            System.out.println("🏰 Création de la guilde...");
            String guildJson = String.format("{\"name\":\"Ma Guilde\",\"description\":\"Guilde de test\",\"owner\":{\"id\":%d}}", adminId);
            long guildId = Long.parseLong(post("/guilds", guildJson));
            System.out.println("✅ Guilde créée (ID: " + guildId + ")\n");
            
            // 3. Créer les channels
            System.out.println("📢 Création des channels...");
            String channel1Json = String.format("{\"name\":\"general\",\"type\":\"TEXT\",\"guild\":{\"id\":%d}}", guildId);
            String channel2Json = String.format("{\"name\":\"announcements\",\"type\":\"TEXT\",\"guild\":{\"id\":%d}}", guildId);
            long channel1Id = Long.parseLong(post("/channels", channel1Json));
            long channel2Id = Long.parseLong(post("/channels", channel2Json));
            System.out.println("✅ 2 channels créés\n");
            
            // 4. Créer les rôles
            System.out.println("🎭 Création des rôles...");
            String role1Json = String.format("{\"name\":\"Admin\",\"color\":\"#FF0000\",\"permissions\":[\"MANAGE_GUILD\",\"KICK_MEMBERS\"],\"guild\":{\"id\":%d}}", guildId);
            String role2Json = String.format("{\"name\":\"Member\",\"color\":\"#00FF00\",\"permissions\":[\"SEND_MESSAGES\"],\"guild\":{\"id\":%d}}", guildId);
            long role1Id = Long.parseLong(post("/roles", role1Json));
            long role2Id = Long.parseLong(post("/roles", role2Json));
            System.out.println("✅ 2 rôles créés\n");
            
            // 5. Créer les messages
            System.out.println("💬 Création des messages...");
            String msg1Json = String.format("{\"content\":\"Bienvenue sur le serveur!\",\"author\":{\"id\":%d},\"channel\":{\"id\":%d}}", adminId, channel1Id);
            String msg2Json = String.format("{\"content\":\"Merci pour l'accueil!\",\"author\":{\"id\":%d},\"channel\":{\"id\":%d}}", user1Id, channel1Id);
            String msg3Json = String.format("{\"content\":\"Annonce importante\",\"author\":{\"id\":%d},\"channel\":{\"id\":%d}}", adminId, channel2Id);
            post("/messages", msg1Json);
            post("/messages", msg2Json);
            post("/messages", msg3Json);
            System.out.println("✅ 3 messages créés\n");
            
            System.out.println("===========================================");
            System.out.println("✅ Seeding terminé avec succès !");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static long createUser(String username, String email, String avatar) throws IOException, InterruptedException {
        String json = String.format("{\"username\":\"%s\",\"discriminator\":\"0001\",\"email\":\"%s\",\"avatarUrl\":\"%s\"}", username, email, avatar);
        String response = post("/users", json);
        return Long.parseLong(response);
    }
    
    private static String post(String endpoint, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Extraire l'ID de la réponse JSON (simplifié - suppose format {"id":123,...})
            String body = response.body();
            if (body.contains("\"id\":")) {
                int idStart = body.indexOf("\"id\":") + 5;
                int idEnd = body.indexOf(",", idStart);
                if (idEnd == -1) {
                    idEnd = body.indexOf("}", idStart);
                }
                return body.substring(idStart, idEnd).trim();
            }
            return "0";
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}
