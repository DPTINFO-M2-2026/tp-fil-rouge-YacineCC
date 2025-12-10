package fr.univtln.yhaouas846.discord4j;

import fr.univtln.yhaouas846.discord4j.services.LangChain4jClient;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
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

public class ThrasherBotReactive {

    private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
    private static final String HELLBOMB_DIRECTORY = "discord-bot-resources/thrasher-hellbomb";
    private static final Random random = new Random();

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN"); // Ton token Discord
        DiscordClient client = DiscordClient.create(token);

        // Initialise LangChain4j/Ollama
        LangChain4jClient ollama = new LangChain4jClient();

        // Vérifie les dossiers Thrasher
        File coversDir = new File(COVERS_DIRECTORY);
        File hellbombDir = new File(HELLBOMB_DIRECTORY);

        GatewayDiscordClient gateway = client.login().block();
        if (gateway == null) {
            System.err.println("❌ Erreur : impossible de se connecter à Discord");
            return;
        }

        System.out.println("✅ Bot connecté");

        gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();

            // Ignore les messages du bot
            if (message.getAuthor().map(User::isBot).orElse(false)) return Mono.empty();

            String content = message.getContent().trim();

            // ------------------- COMMANDES LLM -------------------
            if (content.startsWith("/ask ")) {
                String userInput = content.substring(5);
                String response = ollama.ask(userInput);
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage(response))
                        .then();
            }

            if (content.startsWith("/translate ")) {
                String userInput = content.substring(11);
                String response = ollama.translate(userInput);
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage(response))
                        .then();
            }

            // ------------------- COMMANDES THRASHER -------------------
            if (content.equalsIgnoreCase("!thrasher")) {
                return handleThrasherCommand(message, coversDir);
            } else if (content.equalsIgnoreCase("!thrasher hellbomb")) {
                return handleHellbombCommand(message, hellbombDir);
            } else if (content.equalsIgnoreCase("!thrasher help")) {
                return handleHelpCommand(message);
            }

            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().block();
    }

    // ------------------- THRASHER COMMANDES -------------------
    private static Mono<Void> handleThrasherCommand(Message message, File coversDir) {
        File[] images = getImageFiles(coversDir);
        if (images.length == 0) {
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Aucune cover disponible !"))
                    .then();
        }

        File randomImage = images[random.nextInt(images.length)];
        try {
            byte[] imageData = Files.readAllBytes(randomImage.toPath());
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageData);
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage(MessageCreateSpec.builder()
                            .content("🛹 **Thrasher Magazine Cover** 🛹")
                            .addFile(randomImage.getName(), imageStream)
                            .build()))
                    .then();
        } catch (IOException e) {
            e.printStackTrace();
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Erreur lors du chargement de l'image !"))
                    .then();
        }
    }

    private static Mono<Void> handleHellbombCommand(Message message, File hellbombDir) {
        File[] videos = getVideoFiles(hellbombDir);
        if (videos.length == 0) {
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Aucune vidéo Hellbomb disponible !"))
                    .then();
        }

        File randomVideo = videos[random.nextInt(videos.length)];
        try {
            byte[] videoData = Files.readAllBytes(randomVideo.toPath());
            ByteArrayInputStream videoStream = new ByteArrayInputStream(videoData);
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage(MessageCreateSpec.builder()
                            .content("💣 **HELLBOMB!** 💣")
                            .addFile(randomVideo.getName(), videoStream)
                            .build()))
                    .then();
        } catch (IOException e) {
            e.printStackTrace();
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Erreur lors du chargement de la vidéo !"))
                    .then();
        }
    }

    private static Mono<Void> handleHelpCommand(Message message) {
        String help = """
                🛹 **THRASHER BOT - Aide** 🛹
                
                **Commandes disponibles :**
                `!thrasher` - Poste une cover aléatoire
                `!thrasher hellbomb` - Poste une vidéo Hellbomb aléatoire
                `!thrasher help` - Affiche ce message
                
                **Commandes LLM :**
                `/ask <texte>` - Pose une question au modèle
                `/translate <texte>` - Traduit un texte en français
                """;
        return message.getChannel().flatMap(ch -> ch.createMessage(help)).then();
    }

    private static File[] getImageFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$"));
        return files != null ? files : new File[0];
    }

    private static File[] getVideoFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(mp4|mov|avi|webm)$"));
        return files != null ? files : new File[0];
    }
}
