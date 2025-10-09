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
import java.util.Random;

/**
 * Bot Discord qui poste des covers aléatoires de Thrasher Magazine
 * et des vidéos Hellbomb
 * 
 * Commandes:
 * - !thrasher : Poste une cover aléatoire de Thrasher
 * - !thrasher hellbomb : Poste une vidéo Hellbomb aléatoire
 * - !thrasher help : Affiche l'aide
 * 
 * IMPORTANT: 
 * 1. Remplace "TOKEN" par ton vrai token Discord Bot
 * 2. Ajoute des images dans discord-bot-resources/thrasher-covers/
 * 3. Ajoute des vidéos dans discord-bot-resources/thrasher-hellbomb/
 */
public class ThrasherBot {

    private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
    private static final String HELLBOMB_DIRECTORY = "discord-bot-resources/thrasher-hellbomb";
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        String token;
        try {
            token = TokenReader.readToken();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du token : " + e.getMessage());
            return;
        }

        // Vérifier que le dossier existe
        File coversDir = new File(COVERS_DIRECTORY);
        if (!coversDir.exists()) {
            System.err.println("❌ Erreur: Le dossier " + COVERS_DIRECTORY + " n'existe pas !");
            System.err.println("📁 Crée le dossier et ajoute des images de covers Thrasher.");
            return;
        }

        // Vérifier que le dossier hellbomb existe
        File hellbombDir = new File(HELLBOMB_DIRECTORY);
        if (!hellbombDir.exists()) {
            System.err.println("⚠️  Warning: Le dossier " + HELLBOMB_DIRECTORY + " n'existe pas !");
            System.err.println("📁 Crée le dossier et ajoute des vidéos Hellbomb pour utiliser !thrasher hellbomb");
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
            System.out.println("✅ Bot connecté : " + self.getUsername());
            System.out.println("📁 Dossier covers : " + COVERS_DIRECTORY);
            System.out.println("� Dossier hellbomb : " + HELLBOMB_DIRECTORY);
            
            // Compter les images disponibles
            File[] images = getImageFiles(coversDir);
            System.out.println("🖼️  Images disponibles : " + images.length);
            
            // Compter les vidéos disponibles
            File[] videos = getVideoFiles(hellbombDir);
            System.out.println("🎥 Vidéos disponibles : " + videos.length);
            
            if (images.length == 0) {
                System.out.println("⚠️  ATTENTION: Aucune image trouvée !");
                System.out.println("   Ajoute des images (.jpg, .png, .gif) dans " + COVERS_DIRECTORY);
            }
            if (videos.length == 0) {
                System.out.println("⚠️  ATTENTION: Aucune vidéo trouvée !");
                System.out.println("   Ajoute des vidéos (.mp4, .mov, .avi, .webm) dans " + HELLBOMB_DIRECTORY);
            }
            
            if (images.length > 0 || videos.length > 0) {
                System.out.println("📝 Commandes disponibles :");
                if (images.length > 0) {
                    System.out.println("   !thrasher          → Poste une cover aléatoire");
                }
                if (videos.length > 0) {
                    System.out.println("   !thrasher hellbomb → Poste une vidéo Hellbomb aléatoire");
                }
                System.out.println("   !thrasher help     → Affiche l'aide");
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

            // Commande !ask <question>
            if (content.toLowerCase().startsWith("!ask ")) {
                String question = content.substring(5).trim();
                message.getChannel().block().createMessage("⏳ Je demande à l'IA...").block();
                new Thread(() -> {
                    try {
                        LangChain4jClient langChainClient = new LangChain4jClient();
                        String answer = langChainClient.ask(question);
                        System.out.println("Réponse IA : " + answer);
                        message.getChannel().block().createMessage("🤖 IA : " + answer).block();
                    } catch (Exception e) {
                        message.getChannel().block().createMessage("❌ Erreur IA : " + e.getMessage()).block();
                        e.printStackTrace();
                    }
                }).start();
            }
            // Commande !thrasher hellbomb
            else if (content.equalsIgnoreCase("!thrasher hellbomb")) {
                handleHellbombCommand(message, hellbombDir);
            }
            // Commande !thrasher
            else if (content.equalsIgnoreCase("!thrasher")) {
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
            `!thrasher hellbomb` - Poste une vidéo Hellbomb aléatoire 💣
            `!thrasher help` - Affiche ce message d'aide
            
            **Configuration :**
            📁 Dossier des covers : `discord-bot-resources/thrasher-covers/`
            � Dossier des vidéos : `discord-bot-resources/thrasher-hellbomb/`
            �🖼️ Formats images : .jpg, .jpeg, .png, .gif
            🎥 Formats vidéos : .mp4, .mov, .avi, .webm
            
            **Comment ajouter du contenu :**
            1. Télécharge des covers/vidéos de Thrasher
            2. Place-les dans les dossiers appropriés
            3. Lance !thrasher ou !thrasher hellbomb !
            
            ⚠️  Limites Discord : 8 MB (sans Nitro) / 50 MB (avec Nitro)
            
            Skate on! 🛹✨
            """;
        
        message.getChannel().block()
            .createMessage(helpMessage)
            .block();
        
        System.out.println("📖 Aide affichée pour " + 
            message.getAuthor().map(User::getUsername).orElse("Inconnu"));
    }
    
    /**
     * Gère la commande !thrasher hellbomb - Poste une vidéo aléatoire
     */
    private static void handleHellbombCommand(Message message, File hellbombDir) {
        try {
            // Vérifier que le dossier existe
            if (!hellbombDir.exists()) {
                message.getChannel().block()
                    .createMessage("❌ Le dossier des vidéos Hellbomb n'existe pas !\n" +
                                 "� Crée le dossier `" + HELLBOMB_DIRECTORY + "`")
                    .block();
                System.out.println("⚠️  Dossier hellbomb inexistant");
                return;
            }
            
            File[] videos = getVideoFiles(hellbombDir);
            
            if (videos.length == 0) {
                message.getChannel().block()
                    .createMessage("❌ Aucune vidéo Hellbomb disponible !\n" +
                                 "📁 Ajoute des vidéos (.mp4, .mov, .avi, .webm) dans `" + HELLBOMB_DIRECTORY + "`\n" +
                                 "⚠️  Attention : Limite Discord = 8 MB (sans Nitro) / 50 MB (avec Nitro)")
                    .block();
                System.out.println("⚠️  Aucune vidéo disponible pour la commande !thrasher hellbomb");
                return;
            }
            
            // Choisir une vidéo aléatoire
            File randomVideo = videos[random.nextInt(videos.length)];
            
            // Vérifier la taille du fichier (limite Discord : 8 MB par défaut)
            long fileSizeMB = randomVideo.length() / (1024 * 1024);
            if (fileSizeMB > 8) {
                System.out.println("⚠️  ATTENTION: La vidéo " + randomVideo.getName() + 
                                 " fait " + fileSizeMB + " MB (> 8 MB limite Discord)");
                message.getChannel().block()
                    .createMessage("⚠️  La vidéo sélectionnée (" + randomVideo.getName() + 
                                 ") est trop grande (" + fileSizeMB + " MB).\n" +
                                 "Limite Discord : 8 MB (sans Nitro) / 50 MB (avec Nitro)\n" +
                                 "💡 Compresse la vidéo ou sélectionne-en une autre.")
                    .block();
                return;
            }
            
            System.out.println("🎥 Envoi de la vidéo Hellbomb : " + randomVideo.getName() + 
                             " (" + fileSizeMB + " MB)");
            
            // Envoyer la vidéo avec un message
            try (FileInputStream videoStream = new FileInputStream(randomVideo)) {
                message.getChannel().block()
                    .createMessage(MessageCreateSpec.builder()
                        .content("💣 **HELLBOMB!** 💣\n🛹 Thrasher Magazine")
                        .addFile(randomVideo.getName(), videoStream)
                        .build())
                    .block();
                
                System.out.println("✅ Vidéo Hellbomb envoyée avec succès !");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi de la vidéo : " + e.getMessage());
            message.getChannel().block()
                .createMessage("❌ Erreur lors du chargement de la vidéo !")
                .block();
        }
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
    
    /**
     * Récupère tous les fichiers vidéos du dossier
     */
    private static File[] getVideoFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".mp4") || 
                   lowerName.endsWith(".mov") || 
                   lowerName.endsWith(".avi") || 
                   lowerName.endsWith(".webm");
        });
        
        return files != null ? files : new File[0];
    }
}
