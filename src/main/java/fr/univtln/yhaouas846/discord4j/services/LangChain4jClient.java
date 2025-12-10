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

public class LangChain4jClient {
    private static final String DEFAULT_HOST_IP = "192.168.56.1";
    private static final int DEFAULT_OLLAMA_PORT = 11434;
    private static final String DEFAULT_MODEL_NAME = "llama3.2:1b";
    
    private final ChatModel chatModel;

    public LangChain4jClient() {
        Properties props = loadProperties();
        String hostIp = props.getProperty("LANGCHAIN4J_HOST_IP", DEFAULT_HOST_IP);
        int port = Integer.parseInt(props.getProperty("LANGCHAIN4J_OLLAMA_PORT", String.valueOf(DEFAULT_OLLAMA_PORT)));
        String modelName = props.getProperty("LANGCHAIN4J_MODEL_NAME", DEFAULT_MODEL_NAME);
        
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://" + hostIp + ":" + port)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
                
        System.out.println("LangChain4J connecté à: http://" + hostIp + ":" + port + " (modèle: " + modelName + ")");
    }

    public LangChain4jClient(String modelName) {
        Properties props = loadProperties();
        String hostIp = props.getProperty("LANGCHAIN4J_HOST_IP", DEFAULT_HOST_IP);
        int port = Integer.parseInt(props.getProperty("LANGCHAIN4J_OLLAMA_PORT", String.valueOf(DEFAULT_OLLAMA_PORT)));
        
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://" + hostIp + ":" + port)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
                
        System.out.println("LangChain4J connecté à: http://" + hostIp + ":" + port + " (modèle: " + modelName + ")");
    }

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

    public String ask(String question) {
        try {
            return this.chatModel.chat(UserMessage.from(question)).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur LangChain4J: " + e.getMessage();
        }
    }

        /**
     * Traduction d'un message en français en utilisant un message système
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
