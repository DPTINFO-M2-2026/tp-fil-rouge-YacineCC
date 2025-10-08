package fr.univtln.yhaouas846.discord4j;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bot Discord qui poste des covers aléatoires de Thrasher Magazine
 * 
 * Commandes:
 * - !thrasher : Poste une cover aléatoire de Thrasher
 * - !thrasher help : Affiche l'aide
 * 
 * IMPORTANT: 
 * 1. Remplace "TOKEN" par ton vrai token Discord Bot
 * 2. Ajoute des images dans discord-bot-resources/thrasher-covers/
 */
public class ThrasherBot {

    private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        // REMPLACE "TOKEN" PAR TON VRAI TOKEN DISCORD BOT
        String token = "TOKEN";
        
        // Vérifier que le dossier existe
        File coversDir = new File(COVERS_DIRECTORY);
        if (!coversDir.exists()) {
            System.err.println("❌ Erreur: Le dossier " + COVERS_DIRECTORY + " n'existe pas !");
            System.err.println("📁 Crée le dossier et ajoute des images de covers Thrasher.");
            return;
        }
        
        // Connexion au bot Discord
        DiscordClient client = DiscordClient.create(token);
        GatewayDiscordClient gateway = client.login().block();
        
        System.out.println("════════════════════════════════════");
        System.out.println("  🛹 Thrasher Bot Démarré 🛹");
        System.out.println("════════════════════════════════════");
        
        // Event: Quand le bot est prêt
        gateway.on(ReadyEvent.class).subscribe(event -> {
            User self = event.getSelf();
            System.out.println("✅ Bot connecté : " + self.getUsername() + "#" + self.getDiscriminator());
            System.out.println("📁 Dossier covers : " + COVERS_DIRECTORY);
            
            // Compter les images disponibles
            File[] images = getImageFiles(coversDir);
            System.out.println("🖼️  Images disponibles : " + images.length);
            
            if (images.length == 0) {
                System.out.println("⚠️  ATTENTION: Aucune image trouvée !");
                System.out.println("   Ajoute des images (.jpg, .png, .gif) dans " + COVERS_DIRECTORY);
            } else {
                System.out.println("📝 Commandes disponibles :");
                System.out.println("   !thrasher      → Poste une cover aléatoire");
                System.out.println("   !thrasher help → Affiche l'aide");
            }
            System.out.println("════════════════════════════════════");
        });
        
        // Event: Quand un message est créé
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            
            // Ignore les messages du bot lui-même
            if (message.getAuthor().map(User::isBot).orElse(false)) {
                return;
            }
            
            String content = message.getContent().trim();
            
            // Commande !thrasher
            if (content.equalsIgnoreCase("!thrasher")) {
                handleThrasherCommand(message, coversDir);
            }
            // Commande !thrasher help
            else if (content.equalsIgnoreCase("!thrasher help")) {
                handleHelpCommand(message);
            }
        });
        
        // Garder le bot en vie
        gateway.onDisconnect().block();
    }
    
    /**
     * Gère la commande !thrasher - Poste une cover aléatoire
     */
    private static void handleThrasherCommand(Message message, File coversDir) {
        try {
            File[] images = getImageFiles(coversDir);
            
            if (images.length == 0) {
                message.getChannel().block()
                    .createMessage("❌ Aucune cover Thrasher disponible !\n" +
                                 "📁 Ajoute des images dans `" + COVERS_DIRECTORY + "`")
                    .block();
                System.out.println("⚠️  Aucune image disponible pour la commande !thrasher");
                return;
            }
            
            // Choisir une image aléatoire
            File randomImage = images[random.nextInt(images.length)];
            
            System.out.println("🖼️  Envoi de la cover : " + randomImage.getName());
            
            // Envoyer l'image avec un message
            try (FileInputStream imageStream = new FileInputStream(randomImage)) {
                message.getChannel().block()
                    .createMessage(MessageCreateSpec.builder()
                        .content("🛹 **Thrasher Magazine Cover** 🛹")
                        .addFile(randomImage.getName(), imageStream)
                        .build())
                    .block();
                
                System.out.println("✅ Cover envoyée avec succès !");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'image : " + e.getMessage());
            message.getChannel().block()
                .createMessage("❌ Erreur lors du chargement de l'image !")
                .block();
        }
    }
    
    /**
     * Gère la commande !thrasher help - Affiche l'aide
     */
    private static void handleHelpCommand(Message message) {
        String helpMessage = """
            🛹 **THRASHER BOT - Aide** 🛹
            ═══════════════════════════════
            
            **Commandes disponibles :**
            
            `!thrasher` - Poste une cover aléatoire de Thrasher Magazine
            `!thrasher help` - Affiche ce message d'aide
            
            **Configuration :**
            📁 Dossier des covers : `discord-bot-resources/thrasher-covers/`
            🖼️ Formats supportés : .jpg, .jpeg, .png, .gif
            
            **Comment ajouter des covers :**
            1. Télécharge des covers de Thrasher
            2. Place-les dans le dossier `discord-bot-resources/thrasher-covers/`
            3. Lance !thrasher pour en voir une au hasard !
            
            Skate on! 🛹✨
            """;
        
        message.getChannel().block()
            .createMessage(helpMessage)
            .block();
        
        System.out.println("📖 Aide affichée pour " + 
            message.getAuthor().map(User::getUsername).orElse("Inconnu"));
    }
    
    /**
     * Récupère tous les fichiers images du dossier
     */
    private static File[] getImageFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || 
                   lowerName.endsWith(".jpeg") || 
                   lowerName.endsWith(".png") || 
                   lowerName.endsWith(".gif");
        });
        
        return files != null ? files : new File[0];
    }
}
