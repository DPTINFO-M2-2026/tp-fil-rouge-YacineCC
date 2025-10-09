package fr.univtln.yhaouas846.discord4j;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

/**
 * Bot Discord simple pour tester Discord4J
 * Répond "pong !" quand on écrit "ping ?"
 * 
 * IMPORTANT: Remplace "TOKEN" par ton vrai token Discord Bot
 * Pour obtenir un token:
 * 1. Va sur https://discord.com/developers/applications
 * 2. Crée une nouvelle application
 * 3. Va dans "Bot" et clique sur "Add Bot"
 * 4. Copie le token
 * 5. Active "MESSAGE CONTENT INTENT" dans Bot > Privileged Gateway Intents
 */
public class MyBot {

    public static void main(String[] args) {
        String token;
        try {
            token = TokenReader.readToken();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du token : " + e.getMessage());
            return;
        }

        // Création du client Discord
        DiscordClient client = DiscordClient.create(token);

        // Connexion et récupération du client gateway
        GatewayDiscordClient gateway = client.login().block();

        System.out.println("======================");
        System.out.println("  Bot Discord Démarré");
        System.out.println("======================");

        // Event: Quand le bot est prêt
        gateway.on(ReadyEvent.class).subscribe(event -> {
            User self = event.getSelf();
            System.out.println("✅ Bot connecté : " + self.getUsername() + "#" + self.getDiscriminator());
            System.out.println("📝 En attente de messages 'ping ?'...");
        });
        
        // Event: Quand un message est créé
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            
            // Ignore les messages du bot lui-même
            if (message.getAuthor().map(user -> user.isBot()).orElse(false)) {
                return;
            }
            
            String content = message.getContent();
            
            // Si le message est "ping ?"
            if (content.equalsIgnoreCase("ping ?") || content.equalsIgnoreCase("ping?")) {
                System.out.println("📨 Message reçu: " + content + " de " + 
                    message.getAuthor().map(User::getUsername).orElse("Inconnu"));
                
                // Répondre "pong !"
                message.getChannel().block().createMessage("pong !").block();
                
                System.out.println("✉️  Réponse envoyée: pong !");
            }
        });
        
        // Garder le bot en vie
        gateway.onDisconnect().block();
    }
}
