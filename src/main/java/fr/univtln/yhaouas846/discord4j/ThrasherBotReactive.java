package fr.univtln.yhaouas846.discord4j;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Bot Discord qui poste des covers aléatoires de Thrasher Magazine
 * et des vidéos Hellbomb - VERSION REACTIVE avec Mono
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
public class ThrasherBotReactive {

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
        
        // Connexion au bot Discord avec withGateway (pattern réactif)
        DiscordClient client = DiscordClient.create(token);
        
        System.out.println("════════════════════════════════════");
        System.out.println("  🛹 Thrasher Bot Démarré 🛹");
        System.out.println("════════════════════════════════════");
        
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            // Event: Quand le bot est prêt (Reactive avec Mono)
            Mono<Void> printOnReady = gateway.on(ReadyEvent.class, event -> 
                Mono.fromRunnable(() -> {
                    User self = event.getSelf();
                    System.out.println("✅ Bot connecté : " + self.getUsername());
                    System.out.println("📁 Dossier covers : " + COVERS_DIRECTORY);
                    System.out.println("📂 Dossier hellbomb : " + HELLBOMB_DIRECTORY);
                    
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
                })
            ).then();
            
            // Event: Quand un message est créé (Reactive avec Mono)
            Mono<Void> handleCommands = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
                
                // Ignore les messages du bot lui-même
                if (message.getAuthor().map(User::isBot).orElse(false)) {
                    return Mono.empty();
                }
                
                String content = message.getContent().trim();
                
                // Commande !thrasher hellbomb
                if (content.equalsIgnoreCase("!thrasher hellbomb")) {
                    return handleHellbombCommand(message, hellbombDir);
                }
                // Commande !thrasher
                else if (content.equalsIgnoreCase("!thrasher")) {
                    return handleThrasherCommand(message, coversDir);
                }
                // Commande !thrasher help
                else if (content.equalsIgnoreCase("!thrasher help")) {
                    return handleHelpCommand(message);
                }
                // Commande !ask <question>
                else if (content.toLowerCase().startsWith("!ask ")) {
                    String question = content.substring(5).trim();
                    return message.getChannel().flatMap(channel ->
                        channel.createMessage("⏳ Je demande à Mistral AI...")
                    ).then(
                        Mono.fromCallable(() -> {
                            try {
                                String mistralToken = MistralTokenReader.readToken();
                                MistralClient mistralClient = new MistralClient(mistralToken);
                                String response = mistralClient.ask(question);
                                System.out.println("Réponse Mistral brute : " + response);
                                String answer = extractMistralAnswer(response);
                                return answer;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "❌ Erreur Mistral AI : " + e.getMessage();
                            }
                        })
                        .flatMap(answer -> message.getChannel().flatMap(channel -> channel.createMessage("🤖 Mistral AI : " + answer)))
                        .then()
                    );
                }
                
                return Mono.empty();
            }).then();
            
            // Combine tous les événements
            return printOnReady.and(handleCommands);
        });
        
        // Bloquer pour garder le bot en vie
        login.block();
    }
    
    /**
     * Gère la commande !thrasher - Poste une cover aléatoire (VERSION REACTIVE)
     */
    private static Mono<Void> handleThrasherCommand(Message message, File coversDir) {
        File[] images = getImageFiles(coversDir);
        
        if (images.length == 0) {
            System.out.println("⚠️  Aucune image disponible pour la commande !thrasher");
            return message.getChannel()
                .flatMap(channel -> channel.createMessage("❌ Aucune cover Thrasher disponible !\n" +
                                                         "📁 Ajoute des images dans `" + COVERS_DIRECTORY + "`"))
                .then();
        }
        
        // Choisir une image aléatoire
        File randomImage = images[random.nextInt(images.length)];
        System.out.println("🖼️  Envoi de la cover : " + randomImage.getName());
        
        return message.getChannel()
            .flatMap(channel -> {
                try {
                    // Lire le fichier complet en mémoire puis créer un stream (plus fiable en réactif)
                    byte[] imageData = Files.readAllBytes(randomImage.toPath());
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageData);
                    return channel.createMessage(MessageCreateSpec.builder()
                        .content("🛹 **Thrasher Magazine Cover** 🛹")
                        .addFile(randomImage.getName(), imageStream)
                        .build())
                        .doOnSuccess(msg -> System.out.println("✅ Cover envoyée avec succès !"));
                } catch (IOException e) {
                    System.err.println("❌ Erreur lors de l'envoi de l'image : " + e.getMessage());
                    e.printStackTrace();
                    return channel.createMessage("❌ Erreur lors du chargement de l'image !");
                }
            })
            .then();
    }
    
    /**
     * Gère la commande !thrasher help - Affiche l'aide (VERSION REACTIVE)
     */
    private static Mono<Void> handleHelpCommand(Message message) {
        String helpMessage = """
            🛹 **THRASHER BOT - Aide** 🛹
            ═══════════════════════════════
            
            **Commandes disponibles :**
            
            `!thrasher` - Poste une cover aléatoire de Thrasher Magazine
            `!thrasher hellbomb` - Poste une vidéo Hellbomb aléatoire 💣
            `!thrasher help` - Affiche ce message d'aide
            
            **Configuration :**
            📁 Dossier des covers : `discord-bot-resources/thrasher-covers/`
            📂 Dossier des vidéos : `discord-bot-resources/thrasher-hellbomb/`
            🖼️ Formats images : .jpg, .jpeg, .png, .gif
            🎥 Formats vidéos : .mp4, .mov, .avi, .webm
            
            **Comment ajouter du contenu :**
            1. Télécharge des covers/vidéos de Thrasher
            2. Place-les dans les dossiers appropriés
            3. Lance !thrasher ou !thrasher hellbomb !
            
            ⚠️  Limites Discord : 8 MB (sans Nitro) / 50 MB (avec Nitro)
            
            Skate on! 🛹✨
            """;
        
        System.out.println("📖 Aide affichée pour " + 
            message.getAuthor().map(User::getUsername).orElse("Inconnu"));
        
        return message.getChannel()
            .flatMap(channel -> channel.createMessage(helpMessage))
            .then();
    }
    
    /**
     * Gère la commande !thrasher hellbomb - Poste une vidéo aléatoire (VERSION REACTIVE)
     */
    private static Mono<Void> handleHellbombCommand(Message message, File hellbombDir) {
        // Vérifier que le dossier existe
        if (!hellbombDir.exists()) {
            System.out.println("⚠️  Dossier hellbomb inexistant");
            return message.getChannel()
                .flatMap(channel -> channel.createMessage("❌ Le dossier des vidéos Hellbomb n'existe pas !\n" +
                                                         "📁 Crée le dossier `" + HELLBOMB_DIRECTORY + "`"))
                .then();
        }
        
        File[] videos = getVideoFiles(hellbombDir);
        
        if (videos.length == 0) {
            System.out.println("⚠️  Aucune vidéo disponible pour la commande !thrasher hellbomb");
            return message.getChannel()
                .flatMap(channel -> channel.createMessage("❌ Aucune vidéo Hellbomb disponible !\n" +
                                                         "📁 Ajoute des vidéos (.mp4, .mov, .avi, .webm) dans `" + HELLBOMB_DIRECTORY + "`\n" +
                                                         "⚠️  Attention : Limite Discord = 8 MB (sans Nitro) / 50 MB (avec Nitro)"))
                .then();
        }
        
        // Choisir une vidéo aléatoire
        File randomVideo = videos[random.nextInt(videos.length)];
        
        // Vérifier la taille du fichier (limite Discord : 8 MB par défaut)
        long fileSizeMB = randomVideo.length() / (1024 * 1024);
        if (fileSizeMB > 8) {
            System.out.println("⚠️  ATTENTION: La vidéo " + randomVideo.getName() + 
                             " fait " + fileSizeMB + " MB (> 8 MB limite Discord)");
            return message.getChannel()
                .flatMap(channel -> channel.createMessage("⚠️  La vidéo sélectionnée (" + randomVideo.getName() + 
                                                         ") est trop grande (" + fileSizeMB + " MB).\n" +
                                                         "Limite Discord : 8 MB (sans Nitro) / 50 MB (avec Nitro)\n" +
                                                         "💡 Compresse la vidéo ou sélectionne-en une autre."))
                .then();
        }
        
        System.out.println("🎥 Envoi de la vidéo Hellbomb : " + randomVideo.getName() + 
                         " (" + fileSizeMB + " MB)");
        
        return message.getChannel()
            .flatMap(channel -> {
                try {
                    // Lire le fichier complet en mémoire puis créer un stream (plus fiable en réactif)
                    byte[] videoData = Files.readAllBytes(randomVideo.toPath());
                    ByteArrayInputStream videoStream = new ByteArrayInputStream(videoData);
                    return channel.createMessage(MessageCreateSpec.builder()
                        .content("💣 **HELLBOMB!** 💣\n🛹 Thrasher Magazine")
                        .addFile(randomVideo.getName(), videoStream)
                        .build())
                        .doOnSuccess(msg -> System.out.println("✅ Vidéo Hellbomb envoyée avec succès !"));
                } catch (IOException e) {
                    System.err.println("❌ Erreur lors de l'envoi de la vidéo : " + e.getMessage());
                    e.printStackTrace();
                    return channel.createMessage("❌ Erreur lors du chargement de la vidéo !");
                }
            })
            .then();
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
    
    // Ajoute la méthode d'extraction JSON à la fin de la classe :
    private static String extractMistralAnswer(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray choices = obj.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                return message.getString("content");
            }
            return "(pas de réponse)";
        } catch (Exception e) {
            // Affiche la réponse brute pour debug
            return "(erreur JSON : " + e.getMessage() + ")\nRéponse brute :\n" + json;
        }
    }
}
