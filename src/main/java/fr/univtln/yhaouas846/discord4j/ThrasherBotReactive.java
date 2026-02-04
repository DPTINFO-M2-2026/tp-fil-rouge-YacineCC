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
    private static final String API_URL = "http://localhost:8080/api";

    // Cache pour mapper les IDs Discord (Snowflake) vers les IDs de la base de données (Long)
    private static final Map<Snowflake, Long> userMap = new HashMap<>();
    private static final Map<Snowflake, Long> guildMap = new HashMap<>();
    private static final Map<Snowflake, Long> channelMap = new HashMap<>();

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

            return Mono.empty();
        }).subscribe();

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
                `!admin scan` - Scanne le serveur et sauvegarde les données
                
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
                                                                    // 4. Sauvegarder les Messages (si c'est un TextChannel)
                                                                    if (channel instanceof TextChannel) {
                                                                        System.out.println("📨 Récupération des messages du channel: " + ((TextChannel) channel).getName());
                                                                        return ((TextChannel) channel).getMessagesBefore(Snowflake.of(System.currentTimeMillis()))
                                                                                .take(50) // Limite pour éviter de surcharger
                                                                                .doOnNext(msg -> System.out.println("  -> Message trouvé: " + msg.getContent().substring(0, Math.min(20, msg.getContent().length()))))
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
