package fr.univtln.yhaouas846.discord4j.services;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatModel;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Client minimal pour interagir avec un modèle de chat via LangChain4j + Ollama.
 *
 * <p>Ce composant construit un {@link dev.langchain4j.model.chat.ChatModel} basé sur
 * {@link dev.langchain4j.model.ollama.OllamaChatModel}.</p>
 *
 * <h2>Configuration</h2>
 * <p>La configuration est lue depuis un fichier {@code langchain4j.properties} si présent,
 * sinon des valeurs par défaut sont utilisées :</p>
 * <ul>
 *   <li>{@code LANGCHAIN4J_HOST_IP} (défaut: {@value #DEFAULT_HOST_IP})</li>
 *   <li>{@code LANGCHAIN4J_OLLAMA_PORT} (défaut: {@value #DEFAULT_OLLAMA_PORT})</li>
 *   <li>{@code LANGCHAIN4J_MODEL_NAME} (défaut: {@value #DEFAULT_MODEL_NAME})</li>
 * </ul>
 *
 * <p>Ce client est volontairement simple : en cas d'erreur, il renvoie une chaîne d'erreur
 * plutôt que de propager l'exception.</p>
 */
public class LangChain4jClient {
    private static final String DEFAULT_HOST_IP = "192.168.56.1";
    private static final int DEFAULT_OLLAMA_PORT = 11434;
    private static final String DEFAULT_MODEL_NAME = "llama3.2:1b";
    
    private final ChatModel chatModel;

    public LangChain4jClient() {
        Properties props = loadProperties();
        String hostIp = resolveProperty(props, "LANGCHAIN4J_HOST_IP", DEFAULT_HOST_IP);
        int port = Integer.parseInt(resolveProperty(props, "LANGCHAIN4J_OLLAMA_PORT", String.valueOf(DEFAULT_OLLAMA_PORT)));
        String modelName = resolveProperty(props, "LANGCHAIN4J_MODEL_NAME", DEFAULT_MODEL_NAME);
        
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://" + hostIp + ":" + port)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(120))
                .build();
                
        System.out.println("LangChain4J connecté à: http://" + hostIp + ":" + port + " (modèle: " + modelName + ")");
    }

    public LangChain4jClient(String modelName) {
        Properties props = loadProperties();
        String hostIp = resolveProperty(props, "LANGCHAIN4J_HOST_IP", DEFAULT_HOST_IP);
        int port = Integer.parseInt(resolveProperty(props, "LANGCHAIN4J_OLLAMA_PORT", String.valueOf(DEFAULT_OLLAMA_PORT)));
        
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://" + hostIp + ":" + port)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(120))
                .build();
                
        System.out.println("LangChain4J connecté à: http://" + hostIp + ":" + port + " (modèle: " + modelName + ")");
    }

    /**
     * Charge des propriétés depuis {@code langchain4j.properties} si disponible.
     *
     * <p>Deux emplacements sont tentés :
     * le classpath via {@code ClassLoader#getResourceAsStream} puis une résolution via
     * {@code /langchain4j.properties}.</p>
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("langchain4j.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Impossible de charger langchain4j.properties, utilisation des valeurs par défaut");
        }
        
        // Cherche aussi dans le répertoire racine du projet
        try (InputStream input = LangChain4jClient.class.getResourceAsStream("/langchain4j.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            // Ignore
        }
        
        return props;
    }

    /**
     * Résout une propriété en priorité : variable d'environnement > fichier properties > défaut.
     */
    private static String resolveProperty(Properties props, String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;
        return props.getProperty(key, defaultValue);
    }

    /**
     * Envoie une question au modèle et renvoie la réponse textuelle.
     *
     * @param question question utilisateur
     * @return réponse du modèle, ou message d'erreur en cas d'exception
     */
    public String ask(String question) {
        try {
            return this.chatModel.chat(UserMessage.from(question)).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur LangChain4J: " + e.getMessage();
        }
    }

    /**
     * Traduit un message arbitraire en français en utilisant un message système.
     *
     * @param userMessage texte à traduire
     * @return traduction, ou message d'erreur en cas d'exception
     */
    public String translate(String userMessage) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un traducteur de haut niveau, traduit le message qu'on te donne en français.");
            ChatMessage userMsg = UserMessage.from(userMessage);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de traduction : " + e.getMessage();
        }
    }

    /**
     * Résume un texte (ex: conversation) de manière concise.
     *
     * @param text texte à résumer
     * @return résumé, ou message d'erreur en cas d'exception
     */
    public String summarize(String text) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un assistant expert en résumé. Résume le texte suivant de manière claire et concise, en conservant les points principaux.");
            ChatMessage userMsg = UserMessage.from(text);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de résumé : " + e.getMessage();
        }
    }

    /**
     * Modère un message en détectant le contenu toxique, offensant ou inapproprié.
     *
     * @param message message à analyser
     * @return analyse de modération (toxicité, recommandation)
     */
    public String moderate(String message) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un modérateur IA. Analyse le message suivant et indique s'il est toxique, offensant, spam, ou inapproprié. Réponds avec: [OK] si acceptable, [ATTENTION] si limite, [TOXIQUE] si problématique, suivi d'une brève explication.");
            ChatMessage userMsg = UserMessage.from(message);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de modération : " + e.getMessage();
        }
    }

    /**
     * Analyse le sentiment d'un ensemble de messages.
     *
     * @param messages texte contenant plusieurs messages
     * @return analyse de sentiment (positif, négatif, neutre, ambiance)
     */
    public String analyzeSentiment(String messages) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un expert en analyse de sentiment. Analyse l'ambiance générale des messages suivants et indique le sentiment dominant (positif, négatif, neutre), les émotions principales, et les sujets récurrents.");
            ChatMessage userMsg = UserMessage.from(messages);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur d'analyse : " + e.getMessage();
        }
    }

    /**
     * Détecte automatiquement la langue et traduit en français.
     *
     * @param text texte dans une langue quelconque
     * @return traduction en français avec détection automatique de langue
     */
    public String translateAuto(String text) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un traducteur polyglotte. Détecte automatiquement la langue du texte suivant et traduis-le en français. Indique d'abord la langue détectée, puis la traduction.");
            ChatMessage userMsg = UserMessage.from(text);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de traduction automatique : " + e.getMessage();
        }
    }

    /**
     * Fournit la définition d'un mot ou concept.
     *
     * @param word mot ou expression à définir
     * @return définition détaillée
     */
    public String define(String word) {
        try {
            ChatMessage systemMessage = SystemMessage.from("Tu es un dictionnaire intelligent. Fournis une définition claire, précise et accessible du mot ou concept suivant. Inclus des exemples d'utilisation si pertinent.");
            ChatMessage userMsg = UserMessage.from(word);

            return this.chatModel.chat(systemMessage, userMsg).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de définition : " + e.getMessage();
        }
    }


    public static void main(String[] args) {
        LangChain4jClient client = new LangChain4jClient();
        //String response = client.ask("Bonjour, peux-tu me parler de Java ?");
        String translation = client.translate("O gentle night, whose sable veil doth cloak the world in quietude profound,\n" + //
                        "Mark how the trembling stars do whisper secrets to the wandering winds,\n" + //
                        "As if the very firmament itself conspires to weave a tapestry\n" + //
                        "Of sorrow, mirth, and the relentless passage of fleeting time.\n" + //
                        "\n" + //
                        "Behold, the moon doth linger, pale and wistful, o’er yon silvered waves,\n" + //
                        "Whilst mortal hearts, ensnared by desire and dread alike,\n" + //
                        "Do wrestle with the phantom shadows of ambition, love, and loss,\n" + //
                        "As kings and knaves alike are swept upon the tides of fate most inexorable.\n" + //
                        "\n" + //
                        "Hark! The echo of yesteryear’s lamentations\n" + //
                        "Doth mingle with the mirthful cadence of unseasoned joy,\n" + //
                        "And in this labyrinth of mortal coil,\n" + //
                        "Each soul must don the mask of folly or wisdom, ere the curtain falls.");
        //System.out.println("Réponse: " + response);
        System.out.println("Traduction: " + translation);
    }
}
