package fr.univtln.yhaouas846.discord4j.examples;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

/**
 * Exemples de code du tutoriel Discord4J
 * 
 * Ce fichier est prévu pour que tu puisses coller les exemples
 * de la documentation Discord4J et les tester.
 * 
 * Pour l'utiliser :
 * 1. Colle ton code d'exemple ici
 * 2. Lance avec : mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.examples.TutorialExamples"
 */
public class TutorialExamples {

    public static void main(String[] args) {
        // REMPLACE "TOKEN" PAR TON VRAI TOKEN
        String token = "TOKEN";
        
        DiscordClient client = DiscordClient.create(token);
        GatewayDiscordClient gateway = client.login().block();
        
        System.out.println("🎓 Bot Tutorial démarré !");
        
        // ========================================
        // COLLE TON CODE D'EXEMPLE ICI
        // ========================================
        
        // Exemple de base : Écouter les messages
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            
            // Ton code ici...
            
        });
        
        // ========================================
        
        gateway.onDisconnect().block();
    }
}
