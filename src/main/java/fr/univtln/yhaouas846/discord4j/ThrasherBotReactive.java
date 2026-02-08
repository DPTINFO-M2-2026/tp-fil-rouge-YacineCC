package fr.univtln.yhaouas846.discord4j;

import fr.univtln.yhaouas846.discord4j.services.LangChain4jClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Bot Discord4J réactif (Reactor) combinant :
 * <ul>
 *   <li>des commandes "fun" (Thrasher covers, Hellbomb vidéos) basées sur des fichiers locaux,</li>
 *   <li>des commandes LLM via {@link fr.univtln.yhaouas846.discord4j.services.LangChain4jClient},</li>
 *   <li>une commande admin de scan qui synchronise un serveur Discord vers l'API REST Quarkus.</li>
 * </ul>
 *
 * <h2>Pré-requis</h2>
 * <ul>
 *   <li>Variable d'environnement {@code DISCORD_TOKEN} configurée.</li>
 *   <li>API Quarkus disponible à {@link #API_URL} (par défaut {@code http://localhost:8080/api}).</li>
 *   <li>Ressources locales présentes dans {@link #COVERS_DIRECTORY} et {@link #HELLBOMB_DIRECTORY}.</li>
 * </ul>
 *
 * <h2>Commandes</h2>
 * <ul>
 *   <li>{@code !thrasher} : poste une cover aléatoire</li>
 *   <li>{@code !thrasher hellbomb} : poste une vidéo aléatoire</li>
 *   <li>{@code !thrasher help} : affiche l'aide</li>
 *   <li>{@code !admin scan} : scanne le serveur courant et pousse users/guild/channels/messages vers l'API</li>
 *   <li>{@code /ask ...} / {@code /translate ...} : délégation LLM</li>
 * </ul>
 *
 * <p>Le scan maintient des caches {@code Snowflake -> Long} afin de faire le mapping entre
 * les identifiants Discord et les identifiants DB (renvoyés par l'API).</p>
 */
public class ThrasherBotReactive {

    private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
    private static final String HELLBOMB_DIRECTORY = "discord-bot-resources/thrasher-hellbomb";
    private static final Random random = new Random();

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    // Utilise la variable d'environnement API_HOST ou par défaut le nom du service Docker
    private static final String API_URL = "http://" + System.getenv().getOrDefault("API_HOST", "discord-bot-app") + ":8080/api";

    // Cache pour mapper les IDs Discord (Snowflake) vers les IDs de la base de données (Long)
    private static final Map<Snowflake, Long> userMap = new HashMap<>();
    private static final Map<Snowflake, Long> guildMap = new HashMap<>();
    private static final Map<Snowflake, Long> channelMap = new HashMap<>();
    
    // Configuration du scan automatique (60 sec pour ne pas bloquer les commandes)
    private static final int SCAN_INTERVAL_SECONDS = 60;

    /**
     * Point d'entrée du bot : connexion à Discord, enregistrement des handlers et blocage
     * jusqu'à la déconnexion.
     */
    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN"); // Ton token Discord
        DiscordClient client = DiscordClient.create(token);

        // Initialise LangChain4j/Ollama
        LangChain4jClient ollama = new LangChain4jClient();

        // Vérifie les dossiers Thrasher
        File coversDir = new File(COVERS_DIRECTORY);
        File hellbombDir = new File(HELLBOMB_DIRECTORY);

        GatewayDiscordClient gateway = client.gateway()
                .setEnabledIntents(IntentSet.all())
                .login()
                .block();
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

            if (content.startsWith("/summarize")) {
                return handleSummarize(message, ollama);
            }

            if (content.startsWith("/moderate ")) {
                String userInput = content.substring(10);
                String response = ollama.moderate(userInput);
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("🛡️ **Analyse de modération:**\n" + response))
                        .then();
            }

            if (content.equalsIgnoreCase("/analyze")) {
                return handleAnalyze(message, ollama);
            }

            if (content.startsWith("/translate-auto ")) {
                String userInput = content.substring(16);
                String response = ollama.translateAuto(userInput);
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("🌐 **Traduction automatique:**\n" + response))
                        .then();
            }

            if (content.startsWith("/define ")) {
                String word = content.substring(8);
                String response = ollama.define(word);
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("📖 **Définition:**\n" + response))
                        .then();
            }

            if (content.startsWith("/weather ")) {
                String city = content.substring(9).trim();
                return handleWeather(message, city);
            }

            // ------------------- COMMANDES THRASHER -------------------
            if (content.equalsIgnoreCase("!thrasher")) {
                return handleThrasherCommand(message, coversDir);
            } else if (content.equalsIgnoreCase("!thrasher hellbomb")) {
                return handleHellbombCommand(message, hellbombDir);
            } else if (content.equalsIgnoreCase("!thrasher help")) {
                return handleHelpCommand(message);
            }

            // ------------------- COMMANDES ADMIN -------------------
            if (content.equalsIgnoreCase("!admin scan")) {
                if (message.getGuildId().isPresent()) {
                    return message.getGuild()
                            .flatMap(guild -> scanGuild(guild, message.getChannel()));
                } else {
                    return message.getChannel()
                            .flatMap(ch -> ch.createMessage("❌ Cette commande doit être utilisée dans un serveur."))
                            .then();
                }
            }

            // !admin createGuild <nom>
            if (content.startsWith("!admin createGuild ")) {
                String guildName = content.substring(19).trim();
                System.out.println("🔧 [ADMIN] createGuild demandé: '" + guildName + "' par " + message.getAuthor().map(User::getUsername).orElse("?"));
                String ownerUsername = message.getAuthor().map(User::getUsername).orElse("unknown");
                return handleCreateGuild(message, guildName, ownerUsername);
            }

            // !admin delete <messageId>
            if (content.startsWith("!admin delete ")) {
                String msgIdStr = content.substring(14).trim();
                System.out.println("🔧 [ADMIN] delete demandé: messageId=" + msgIdStr + " par " + message.getAuthor().map(User::getUsername).orElse("?"));
                return handleDeleteMessage(message, msgIdStr);
            }

            // !admin role <add|remove> <@user> <roleName>
            if (content.startsWith("!admin role ")) {
                System.out.println("🔧 [ADMIN] role demandé: '" + content.substring(12).trim() + "' par " + message.getAuthor().map(User::getUsername).orElse("?"));
                return handleRoleCommand(message, content.substring(12).trim());
            }

            return Mono.empty();
        }).subscribe();

        // ------------------- SCAN AUTOMATIQUE -------------------
        System.out.println("🔄 Démarrage du scan automatique (interval: " + SCAN_INTERVAL_SECONDS + " secondes)");
        
        // Flux périodique pour scanner tous les guilds
        reactor.core.publisher.Flux.interval(Duration.ofSeconds(SCAN_INTERVAL_SECONDS))
                .flatMap(tick -> {
                    System.out.println("\n⏰ [Scan #" + tick + "] Début du scan automatique de tous les serveurs...");
                    return gateway.getGuilds()
                            .flatMap(guild -> {
                                System.out.println("🔍 Scan du serveur: " + guild.getName() + " (ID: " + guild.getId().asString() + ")");
                                return scanGuildSilent(guild, gateway);
                            })
                            .doOnComplete(() -> System.out.println("✅ [Scan #" + tick + "] Scan automatique terminé\n"))
                            .onErrorResume(e -> {
                                System.err.println("❌ Erreur durant le scan automatique: " + e.getMessage());
                                return Mono.empty();
                            });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        gateway.onDisconnect().block();
    }

    // ------------------- THRASHER COMMANDES -------------------
    /**
     * Commande {@code !thrasher} : envoie une cover au hasard depuis le répertoire configuré.
     */
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

    /**
     * Commande {@code !thrasher hellbomb} : envoie une vidéo au hasard depuis le répertoire configuré.
     */
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

    /**
     * Commande {@code !thrasher help} : affiche une aide synthétique.
     */
    private static Mono<Void> handleHelpCommand(Message message) {
        String help = """
                🛹 **THRASHER BOT - Aide** 🛹
                
                **Commandes disponibles :**
                `!thrasher` - Poste une cover aléatoire
                `!thrasher hellbomb` - Poste une vidéo Hellbomb aléatoire
                `!thrasher help` - Affiche ce message
                
                **Commandes Admin :**
                `!admin scan` - Scanne manuellement le serveur et sauvegarde les données
                `!admin createGuild <nom>` - Crée une guilde avec canaux et rôles par défaut
                `!admin delete <messageId>` - Supprime un message (avec vérif de permissions)
                `!admin role add <@user> <rôle>` - Assigne un rôle à un utilisateur
                `!admin role remove <@user> <rôle>` - Retire un rôle d'un utilisateur
                
                **Scan automatique :** 🔄
                Le bot scanne automatiquement tous les serveurs toutes les 5 secondes
                pour persister: Users, Guilds, Channels, Roles et Messages dans la DB.
                
                **Commandes LLM :**
                `/ask <texte>` - Pose une question au modèle
                `/translate <texte>` - Traduit un texte en français
                `/summarize` - Résume les 20 derniers messages du salon
                `/moderate <message>` - Analyse la toxicité d'un message
                `/analyze` - Analyse le sentiment des 30 derniers messages
                `/translate-auto <texte>` - Traduction avec détection auto de langue
                `/define <mot>` - Définition d'un mot ou concept
                `/weather <ville>` - Météo d'une ville
                """;
        return message.getChannel().flatMap(ch -> ch.createMessage(help)).then();
    }

    /**
     * Commande {@code /summarize} : résume les derniers messages du salon.
     */
    private static Mono<Void> handleSummarize(Message message, LangChain4jClient ollama) {
        return message.getChannel()
                .flatMap(channel -> channel.createMessage("⏳ Analyse en cours, veuillez patienter...").then(Mono.just(channel)))
                .flatMap(channel -> {
                    if (channel instanceof TextChannel) {
                        return ((TextChannel) channel).getMessagesBefore(message.getId())
                                .take(20)
                                .map(msg -> {
                                    String author = msg.getAuthor().map(User::getUsername).orElse("Unknown");
                                    return author + ": " + msg.getContent();
                                })
                                .filter(content -> !content.endsWith(": ")) // Ignore messages vides
                                .collectList()
                                .flatMap(messagesList -> {
                                    if (messagesList.isEmpty()) {
                                        return channel.createMessage("❌ Aucun message à résumer.");
                                    }
                                    
                                    String messagesText = String.join("\n", messagesList);
                                    String summary = ollama.summarize(messagesText);
                                    return channel.createMessage("📝 **Résumé des derniers messages:**\n" + summary);
                                })
                                .then();
                    } else {
                        return channel.createMessage("❌ Cette commande fonctionne uniquement dans les salons texte.").then();
                    }
                });
    }

    /**
     * Commande {@code /analyze} : analyse le sentiment des derniers messages.
     */
    private static Mono<Void> handleAnalyze(Message message, LangChain4jClient ollama) {
        return message.getChannel()
                .flatMap(channel -> channel.createMessage("⏳ Analyse en cours, cela peut prendre jusqu'à 2 minutes...").then(Mono.just(channel)))
                .flatMap(channel -> {
                    if (channel instanceof TextChannel) {
                        return ((TextChannel) channel).getMessagesBefore(message.getId())
                                .take(30)
                                .map(msg -> {
                                    String author = msg.getAuthor().map(User::getUsername).orElse("Unknown");
                                    return author + ": " + msg.getContent();
                                })
                                .filter(content -> !content.endsWith(": "))
                                .collectList()
                                .flatMap(messagesList -> {
                                    if (messagesList.isEmpty()) {
                                        return channel.createMessage("❌ Aucun message à analyser.");
                                    }
                                    
                                    String messagesText = String.join("\n", messagesList);
                                    String analysis = ollama.analyzeSentiment(messagesText);
                                    return channel.createMessage("📊 **Analyse de sentiment:**\n" + analysis);
                                })
                                .then();
                    } else {
                        return channel.createMessage("❌ Cette commande fonctionne uniquement dans les salons texte.").then();
                    }
                });
    }

    /**
     * Commande {@code /weather} : récupère la météo via wttr.in API.
     */
    private static Mono<Void> handleWeather(Message message, String city) {
        return message.getChannel()
                .flatMap(ch -> ch.createMessage("⏳ Récupération de la météo pour " + city + "..."))
                .then(Mono.fromCallable(() -> {
                    try {
                        // API wttr.in - format texte simple avec émojis
                        String url = "https://wttr.in/" + city.replace(" ", "+") + "?0&T&Q&lang=fr";
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("User-Agent", "curl/7.68.0")
                                .header("Accept-Language", "fr")
                                .timeout(Duration.ofSeconds(10))
                                .GET()
                                .build();
                        
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200 && !response.body().isEmpty()) {
                            // Nettoyer et formater la réponse
                            String weather = response.body().trim();
                            // Limiter à 5 premières lignes pour éviter trop de texte
                            String[] lines = weather.split("\n");
                            StringBuilder result = new StringBuilder("🌤️ **Météo pour " + city + ":**\n```\n");
                            for (int i = 0; i < Math.min(5, lines.length); i++) {
                                result.append(lines[i]).append("\n");
                            }
                            result.append("```");
                            return result.toString();
                        } else {
                            return "❌ Impossible de récupérer la météo pour: " + city + " (code: " + response.statusCode() + ")";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "❌ Erreur météo: " + e.getMessage();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(weatherText -> message.getChannel().flatMap(ch -> ch.createMessage(weatherText)))
                .then());
    }

    private static File[] getImageFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$"));
        return files != null ? files : new File[0];
    }

    private static File[] getVideoFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(mp4|mov|avi|webm)$"));
        return files != null ? files : new File[0];
    }

    // ------------------- SCAN LOGIC -------------------

    /**
     * Scanne un serveur Discord :
     * <ol>
     *   <li>sauvegarde les membres (users),</li>
     *   <li>sauvegarde la guilde (liée au owner),</li>
     *   <li>sauvegarde les channels,</li>
     *   <li>pour les text channels, récupère une page de messages et les envoie à l'API.</li>
     * </ol>
     *
     * <p>La méthode est tolérante aux erreurs sur certains éléments : elle continue en cas
     * d'échec d'un user/channel/message individuel.</p>
     */
    private static Mono<Void> scanGuild(Guild guild, Mono<discord4j.core.object.entity.channel.MessageChannel> responseChannel) {
        return responseChannel.flatMap(ch -> ch.createMessage("🔄 Début du scan du serveur **" + guild.getName() + "**..."))
                .then(
                        Mono.just(guild)
                                .flatMap(g -> {
                                    System.out.println("Scanning guild: " + g.getName());
                                    // 1. Sauvegarder les membres (Users)
                                    return g.getMembers()
                                            .flatMap(member -> saveUser(member).onErrorResume(e -> Mono.empty())) // Continue même si un user échoue
                                            .then(Mono.just(g));
                                })
                                .flatMap(g -> {
                                    // 2. Sauvegarder la Guilde (nécessite Owner sauvegardé)
                                    return g.getOwner()
                                            .flatMap(owner -> saveGuild(g, owner))
                                            .flatMap(savedGuildId -> {
                                                // 3. Sauvegarder les Channels
                                                return g.getChannels()
                                                        .flatMap(channel -> 
                                                            saveChannel(channel, savedGuildId)
                                                                .flatMap(savedChannelId -> {
                                                                    // 4. Sauvegarder les Messages (si c'est un TextChannel) - 20 par auteur
                                                                    if (channel instanceof TextChannel) {
                                                                        TextChannel textChannel = (TextChannel) channel;
                                                                        System.out.println("📨 Récupération des messages du channel: " + textChannel.getName());
                                                                        return textChannel.getLastMessageId()
                                                                                .map(lastMsgId -> textChannel.getMessagesBefore(lastMsgId))
                                                                                .orElse(reactor.core.publisher.Flux.empty())
                                                                                .take(500) // Récupère beaucoup de messages
                                                                                .collectList()
                                                                                .flatMapMany(messages -> {
                                                                                    // Groupe par auteur et garde 20 par auteur
                                                                                    java.util.Map<Snowflake, java.util.List<Message>> messagesByAuthor = messages.stream()
                                                                                            .filter(msg -> msg.getAuthor().isPresent())
                                                                                            .collect(java.util.stream.Collectors.groupingBy(
                                                                                                    msg -> msg.getAuthor().get().getId()
                                                                                            ));
                                                                                    
                                                                                    java.util.List<Message> top20PerAuthor = messagesByAuthor.values().stream()
                                                                                            .flatMap(authorMessages -> authorMessages.stream()
                                                                                                    .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                                                                                                    .limit(20))
                                                                                            .collect(java.util.stream.Collectors.toList());
                                                                                    
                                                                                    System.out.println("  -> " + top20PerAuthor.size() + " messages à sauvegarder (20 max par auteur)");
                                                                                    return reactor.core.publisher.Flux.fromIterable(top20PerAuthor);
                                                                                })
                                                                                .concatMap(msg -> saveMessage(msg, savedChannelId).onErrorResume(e -> {
                                                                                    System.err.println("Erreur sauvegarde message: " + e.getMessage());
                                                                                    return Mono.empty();
                                                                                }))
                                                                                .collectList()
                                                                                .then();
                                                                    }
                                                                    return Mono.empty();
                                                                })
                                                                .onErrorResume(e -> {
                                                                    System.err.println("Erreur channel " + channel.getId() + ": " + e.getMessage());
                                                                    return Mono.empty();
                                                                })
                                                        )
                                                        .then();
                                            })
                                            .onErrorResume(e -> {
                                                System.err.println("Erreur guild " + g.getName() + ": " + e.getMessage());
                                                return Mono.empty();
                                            });
                                })
                                .then()
                )
                .then(responseChannel.flatMap(ch -> ch.createMessage("✅ Scan terminé avec succès !")).then())
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return responseChannel.flatMap(ch -> ch.createMessage("❌ Erreur durant le scan : " + e.getMessage())).then();
                });
    }

    /**
     * Version silencieuse du scan (sans messages Discord) pour le scan automatique.
     * Scanne un serveur Discord et persiste : users, guild, channels, roles, messages.
     */
    private static Mono<Void> scanGuildSilent(Guild guild, GatewayDiscordClient gateway) {
        return Mono.just(guild)
                .flatMap(g -> {
                    // 1. Sauvegarder les membres (Users)
                    return g.getMembers()
                            .flatMap(member -> saveUser(member).onErrorResume(e -> Mono.empty()))
                            .then(Mono.just(g));
                })
                .flatMap(g -> {
                    // 2. Sauvegarder la Guilde (nécessite Owner sauvegardé)
                    return g.getOwner()
                            .flatMap(owner -> saveGuild(g, owner))
                            .flatMap(savedGuildId -> {
                                // 3. Sauvegarder les Rôles
                                return g.getRoles()
                                        .flatMap(role -> saveRole(role, savedGuildId).onErrorResume(e -> {
                                            System.err.println("Erreur sauvegarde rôle " + role.getName() + ": " + e.getMessage());
                                            return Mono.empty();
                                        }))
                                        .then(Mono.just(savedGuildId));
                            })
                            .flatMap(savedGuildId -> {
                                // 4. Sauvegarder les Channels
                                return g.getChannels()
                                        .flatMap(channel -> 
                                            saveChannel(channel, savedGuildId)
                                                .flatMap(savedChannelId -> {
                                                    // 5. Sauvegarder les Messages (si TextChannel) - 20 par auteur
                                                    if (channel instanceof TextChannel) {
                                                        TextChannel textChannel = (TextChannel) channel;
                                                        System.out.println("📨 Scan messages du channel: " + textChannel.getName());
                                                        // Utilise getLastMessageId() pour obtenir un point de départ valide
                                                        return textChannel.getLastMessageId()
                                                                .map(lastMsgId -> textChannel.getMessagesBefore(lastMsgId))
                                                                .orElse(reactor.core.publisher.Flux.empty()) // Pas de messages si pas de dernier message
                                                                .take(100) // Limite réduite pour éviter de bloquer les commandes
                                                                .collectList()
                                                                .doOnNext(messages -> System.out.println("  📥 " + messages.size() + " messages récupérés"))
                                                                .flatMapMany(messages -> {
                                                                    // Groupe par auteur et garde 20 par auteur
                                                                    java.util.Map<Snowflake, java.util.List<Message>> messagesByAuthor = messages.stream()
                                                                            .filter(msg -> msg.getAuthor().isPresent())
                                                                            .collect(java.util.stream.Collectors.groupingBy(
                                                                                    msg -> msg.getAuthor().get().getId()
                                                                            ));
                                                                    
                                                                    java.util.List<Message> top20PerAuthor = messagesByAuthor.values().stream()
                                                                            .flatMap(authorMessages -> authorMessages.stream()
                                                                                    .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                                                                                    .limit(20))
                                                                            .collect(java.util.stream.Collectors.toList());
                                                                    
                                                                    System.out.println("  💾 " + top20PerAuthor.size() + " messages à sauvegarder (20 max par auteur, " + messagesByAuthor.size() + " auteurs)");
                                                                    return reactor.core.publisher.Flux.fromIterable(top20PerAuthor);
                                                                })
                                                                .concatMap(msg -> saveMessage(msg, savedChannelId).onErrorResume(e -> Mono.empty()))
                                                                .then()
                                                                .doOnSuccess(v -> System.out.println("  ✅ Messages du channel sauvegardés"));
                                                    }
                                                    return Mono.empty();
                                                })
                                                .onErrorResume(e -> {
                                                    System.err.println("❌ Erreur channel " + channel.getId() + ": " + e.getMessage());
                                                    return Mono.empty();
                                                })
                                        )
                                        .then();
                            })
                            .onErrorResume(e -> {
                                System.err.println("Erreur scan guild " + g.getName() + ": " + e.getMessage());
                                return Mono.empty();
                            });
                })
                .then();
    }

    /**
     * Envoie un utilisateur Discord vers l'API et retourne son identifiant DB.
     *
     * <p>Utilise un cache {@link #userMap} pour éviter des créations en double lors du scan.</p>
     */
    private static Mono<Long> saveUser(User discordUser) {
        if (userMap.containsKey(discordUser.getId())) {
            return Mono.just(userMap.get(discordUser.getId()));
        }

        fr.univtln.yhaouas846.projet.entity.User dbUser = new fr.univtln.yhaouas846.projet.entity.User();
        dbUser.username = discordUser.getUsername();
        
        // Gestion des nouveaux usernames (discriminator = "0")
        String discriminator = discordUser.getDiscriminator();
        if ("0".equals(discriminator)) {
            discriminator = "0000";
        }
        dbUser.discriminator = discriminator;
        
        dbUser.email = null;
        dbUser.avatarUrl = discordUser.getAvatarUrl();
        dbUser.createdAt = toLocalDateTime(discordUser.getId().getTimestamp());
        dbUser.isBot = discordUser.isBot();
        dbUser.discordId = discordUser.getId().asString();

        return sendToApi("/users", dbUser)
                .map(json -> {
                    long id = json.get("id").asLong();
                    userMap.put(discordUser.getId(), id);
                    return id;
                })
                .doOnError(e -> System.err.println("❌ Echec sauvegarde User " + discordUser.getUsername() + ": " + e.getMessage()));
    }

    /**
     * Envoie une guilde Discord vers l'API et retourne son identifiant DB.
     *
     * <p>La guilde dépend de l'owner : l'owner est donc sauvegardé/obtenu avant l'envoi.</p>
     */
    private static Mono<Long> saveGuild(Guild discordGuild, User owner) {
        if (guildMap.containsKey(discordGuild.getId())) {
            return Mono.just(guildMap.get(discordGuild.getId()));
        }

        return saveUser(owner).flatMap(ownerId -> {
            fr.univtln.yhaouas846.projet.entity.Guild dbGuild = new fr.univtln.yhaouas846.projet.entity.Guild();
            dbGuild.name = discordGuild.getName();
            dbGuild.description = discordGuild.getDescription().orElse(null);
            dbGuild.iconUrl = discordGuild.getIconUrl(Image.Format.PNG).orElse(null);
            dbGuild.createdAt = toLocalDateTime(discordGuild.getId().getTimestamp());
            
            // Fix: Limite max à 800000 pour respecter la contrainte DB
            int limit = discordGuild.getMaxMembers().orElse(500000);
            dbGuild.memberLimit = Math.min(limit, 800000);
            dbGuild.discordId = discordGuild.getId().asString();
            
            // On lie l'owner par son ID DB
            fr.univtln.yhaouas846.projet.entity.User ownerRef = new fr.univtln.yhaouas846.projet.entity.User();
            ownerRef.id = ownerId;
            dbGuild.owner = ownerRef;

            return sendToApi("/guilds", dbGuild)
                    .map(json -> {
                        long id = json.get("id").asLong();
                        guildMap.put(discordGuild.getId(), id);
                        return id;
                    });
        });
    }

    /**
     * Envoie un channel vers l'API et retourne son identifiant DB.
     *
     * <p>Le payload est envoyé sous forme de {@link java.util.Map} afin de construire une
     * référence de guilde par ID DB et de mapper le type Discord4J vers l'enum interne.</p>
     */
    private static Mono<Long> saveChannel(Channel discordChannel, Long guildId) {
        if (channelMap.containsKey(discordChannel.getId())) {
            return Mono.just(channelMap.get(discordChannel.getId()));
        }

        Map<String, Object> payload = new HashMap<>();
        if (discordChannel instanceof GuildChannel) {
            payload.put("name", ((GuildChannel) discordChannel).getName());
        } else {
            payload.put("name", "channel-" + discordChannel.getId().asString());
        }
        payload.put("description", null);
        payload.put("createdAt", toLocalDateTime(discordChannel.getId().getTimestamp()));
        payload.put("discordId", discordChannel.getId().asString());
        
        // Mapping du type
        fr.univtln.yhaouas846.projet.entity.Channel.ChannelType type;
        switch (discordChannel.getType()) {
            case GUILD_TEXT -> type = fr.univtln.yhaouas846.projet.entity.Channel.ChannelType.TEXT;
            case GUILD_VOICE -> type = fr.univtln.yhaouas846.projet.entity.Channel.ChannelType.VOICE;
            case GUILD_CATEGORY -> type = fr.univtln.yhaouas846.projet.entity.Channel.ChannelType.CATEGORY;
            case GUILD_NEWS -> type = fr.univtln.yhaouas846.projet.entity.Channel.ChannelType.NEWS;
            default -> type = fr.univtln.yhaouas846.projet.entity.Channel.ChannelType.TEXT; // Fallback
        }
        payload.put("type", type);

        Map<String, Object> guildRef = new HashMap<>();
        guildRef.put("id", guildId);
        payload.put("guild", guildRef);

        return sendToApi("/channels", payload)
                .map(json -> {
                    long id = json.get("id").asLong();
                    channelMap.put(discordChannel.getId(), id);
                    return id;
                });
    }

    /**
     * Envoie un rôle Discord vers l'API et retourne son identifiant DB.
     */
    private static Mono<Long> saveRole(discord4j.core.object.entity.Role discordRole, Long guildId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", discordRole.getName());
        // Convertit la couleur RGB en format hexadécimal #RRGGBB
        int rgb = discordRole.getColor().getRGB() & 0xFFFFFF; // Masque pour enlever le canal alpha
        payload.put("color", String.format("#%06X", rgb));
        payload.put("position", discordRole.getRawPosition());
        payload.put("permissions", discordRole.getPermissions().getRawValue());
        payload.put("mentionable", discordRole.isMentionable());
        payload.put("hoisted", discordRole.isHoisted());
        payload.put("discordId", discordRole.getId().asString());

        Map<String, Object> guildRef = new HashMap<>();
        guildRef.put("id", guildId);
        payload.put("guild", guildRef);

        return sendToApi("/roles", payload)
                .map(json -> json.get("id").asLong())
                .doOnSuccess(id -> System.out.println("✅ Rôle sauvegardé: " + discordRole.getName()))
                .doOnError(e -> System.err.println("❌ Erreur rôle " + discordRole.getName() + ": " + e.getMessage()));
    }

    /**
     * Envoie un message vers l'API (si non vide et avec auteur présent).
     *
     * <p>Le message crée des références vers l'auteur et le canal via leurs IDs DB.</p>
     */
    private static Mono<Void> saveMessage(Message discordMessage, Long channelId) {
        // On ignore les messages sans contenu
        if (discordMessage.getContent().isEmpty()) {
            System.out.println("⏭️ Message vide ignoré: " + discordMessage.getId().asString());
            return Mono.empty();
        }

        // Vérifier si l'auteur existe
        if (discordMessage.getAuthor().isEmpty()) {
            System.out.println("⏭️ Message sans auteur ignoré: " + discordMessage.getId().asString());
            return Mono.empty();
        }

        User author = discordMessage.getAuthor().get();
        System.out.println("📝 Sauvegarde message: \"" + discordMessage.getContent().substring(0, Math.min(30, discordMessage.getContent().length())) + "...\" de " + author.getUsername());
        
        return saveUser(author).flatMap(authorId -> {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", discordMessage.getContent());
            payload.put("createdAt", toLocalDateTime(discordMessage.getTimestamp()));
            payload.put("isEdited", discordMessage.getEditedTimestamp().isPresent());
            payload.put("discordId", discordMessage.getId().asString());
            
            Map<String, Object> authorRef = new HashMap<>();
            authorRef.put("id", authorId);
            payload.put("author", authorRef);

            Map<String, Object> channelRef = new HashMap<>();
            channelRef.put("id", channelId);
            payload.put("channel", channelRef);

            return sendToApi("/messages", payload)
                    .doOnSuccess(json -> System.out.println("✅ Message sauvegardé: " + discordMessage.getId().asString()))
                    .doOnError(e -> System.err.println("❌ Erreur message: " + e.getMessage()))
                    .then();
        });
    }

    // ─── Commande !admin createGuild ─────────────────────────────────────────

    /**
     * Crée une guilde avec canaux et rôles par défaut via l'API REST.
     */
    private static Mono<Void> handleCreateGuild(Message message, String guildName, String ownerUsername) {
        return message.getChannel()
                .flatMap(ch -> ch.createMessage("⏳ Création de la guilde **" + guildName + "**..."))
                .then(Mono.fromCallable(() -> {
                    String url = API_URL + "/bot/guilds?name=" +
                            java.net.URLEncoder.encode(guildName, java.nio.charset.StandardCharsets.UTF_8) +
                            "&owner=" + java.net.URLEncoder.encode(ownerUsername, java.nio.charset.StandardCharsets.UTF_8);
                    System.out.println("🔧 [ADMIN] Appel API: POST " + url);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("🔧 [ADMIN] Réponse API: " + response.statusCode() + " - " + response.body().substring(0, Math.min(200, response.body().length())));
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException(objectMapper.readTree(response.body()).path("error").asText("Erreur inconnue"));
                    }
                    return objectMapper.readTree(response.body());
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(json -> message.getChannel().flatMap(ch ->
                        ch.createMessage("✅ Guilde **" + json.path("name").asText() + "** créée !\n" +
                                "📂 Canaux par défaut : general, announcements, General Voice\n" +
                                "🎭 Rôles par défaut : Admin, Member")))
                .then()
                .onErrorResume(e -> {
                    System.err.println("🔧 [ADMIN] ERREUR createGuild: " + e.getClass().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                    return message.getChannel()
                            .flatMap(ch -> ch.createMessage("❌ Erreur : " + e.getMessage())).then();
                });
    }

    // ─── Commande !admin delete ──────────────────────────────────────────────

    /**
     * Supprime un message via l'API REST avec vérification des permissions.
     * L'utilisateur doit être l'auteur du message ou avoir le rôle canManageMessages.
     */
    private static Mono<Void> handleDeleteMessage(Message message, String messageIdStr) {
        return message.getAuthor()
                .map(author -> {
                    Long requesterId = userMap.get(author.getId());
                    if (requesterId == null) {
                        return message.getChannel()
                                .flatMap(ch -> ch.createMessage("❌ Utilisateur non synchronisé. Lancez d'abord `!admin scan`."))
                                .then();
                    }
                    return Mono.fromCallable(() -> {
                        String url = API_URL + "/bot/messages/" + messageIdStr + "?requesterId=" + requesterId;
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .DELETE()
                                .build();
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 403) {
                            throw new SecurityException(objectMapper.readTree(response.body()).path("error").asText("Permission refusée"));
                        }
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException(objectMapper.readTree(response.body()).path("error").asText("Erreur"));
                        }
                        return response.body();
                    }).subscribeOn(Schedulers.boundedElastic())
                    .flatMap(body -> message.getChannel()
                            .flatMap(ch -> ch.createMessage("✅ Message #" + messageIdStr + " supprimé.")))
                    .then()
                    .onErrorResume(SecurityException.class, e -> message.getChannel()
                            .flatMap(ch -> ch.createMessage("🚫 Permission refusée : " + e.getMessage())).then())
                    .onErrorResume(e -> message.getChannel()
                            .flatMap(ch -> ch.createMessage("❌ Erreur : " + e.getMessage())).then());
                })
                .orElse(Mono.empty());
    }

    // ─── Commande !admin role ────────────────────────────────────────────────

    /**
     * Gère l'assignation et le retrait de rôles via l'API REST.
     * Syntaxe : {@code !admin role add @user <roleName>} ou {@code !admin role remove @user <roleName>}
     */
    private static Mono<Void> handleRoleCommand(Message message, String args) {
        String[] parts = args.split("\\s+", 3);
        if (parts.length < 3) {
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Usage : `!admin role <add|remove> <@user> <rôle>`"))
                    .then();
        }

        String action = parts[0].toLowerCase();
        if (!action.equals("add") && !action.equals("remove")) {
            return message.getChannel()
                    .flatMap(ch -> ch.createMessage("❌ Action invalide. Utilisez `add` ou `remove`."))
                    .then();
        }

        // Extraction de l'ID utilisateur depuis la mention <@123456>
        String mention = parts[1];
        String userIdStr = mention.replaceAll("[<@!>]", "");
        String roleName = parts[2];

        return Mono.fromCallable(() -> {
            // Chercher l'utilisateur DB via le discordId
            Snowflake discordUserId = Snowflake.of(userIdStr);
            Long dbUserId = userMap.get(discordUserId);
            if (dbUserId == null) {
                throw new IllegalStateException("Utilisateur non synchronisé. Lancez `!admin scan` d'abord.");
            }

            // Chercher le rôle par nom dans les rôles de la guilde
            String rolesUrl = API_URL + "/roles";
            HttpRequest rolesRequest = HttpRequest.newBuilder()
                    .uri(URI.create(rolesUrl))
                    .GET()
                    .build();
            HttpResponse<String> rolesResp = httpClient.send(rolesRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode roles = objectMapper.readTree(rolesResp.body());

            Long roleId = null;
            for (JsonNode role : roles) {
                if (role.path("name").asText().equalsIgnoreCase(roleName)) {
                    roleId = role.path("id").asLong();
                    break;
                }
            }
            if (roleId == null) {
                throw new IllegalStateException("Rôle '" + roleName + "' introuvable.");
            }

            // POST ou DELETE /api/roles/{roleId}/users/{userId}
            String url = API_URL + "/roles/" + roleId + "/users/" + dbUserId;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");
            HttpRequest request = action.equals("add")
                    ? builder.POST(HttpRequest.BodyPublishers.noBody()).build()
                    : builder.DELETE().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Erreur API (" + response.statusCode() + ")");
            }

            return action.equals("add")
                    ? "✅ Rôle **" + roleName + "** assigné à <@" + userIdStr + ">"
                    : "✅ Rôle **" + roleName + "** retiré de <@" + userIdStr + ">";
        }).subscribeOn(Schedulers.boundedElastic())
        .flatMap(msg -> message.getChannel().flatMap(ch -> ch.createMessage(msg)))
        .then()
        .onErrorResume(e -> message.getChannel()
                .flatMap(ch -> ch.createMessage("❌ " + e.getMessage())).then());
    }

    /**
     * Effectue un POST JSON vers l'API Quarkus.
     *
     * <p>Le call HTTP est exécuté sur {@link reactor.core.scheduler.Schedulers#boundedElastic()}
     * pour éviter de bloquer les threads réactifs.</p>
     *
     * @param path chemin relatif sous {@link #API_URL} (ex: {@code /users})
     * @param entity objet sérialisable JSON (Map ou POJO)
     * @return réponse JSON (body) convertie en {@link com.fasterxml.jackson.databind.JsonNode}
     */
    private static Mono<JsonNode> sendToApi(String path, Object entity) {
        return Mono.fromCallable(() -> {
            String json = objectMapper.writeValueAsString(entity);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                System.err.println("⚠️ Erreur API " + path + " (" + response.statusCode() + "): " + response.body());
                throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readTree(response.body());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Convertit un {@link java.time.Instant} en {@link java.time.LocalDateTime} dans le fuseau
     * par défaut de la JVM.
     */
    private static LocalDateTime toLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
