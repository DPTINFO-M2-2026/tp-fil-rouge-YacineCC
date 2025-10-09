package fr.univtln.yhaouas846.discord4j;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class LangChain4jClient {
    private static final String DEFAULT_HOST_IP = "192.168.56.1";
    private static final int DEFAULT_OLLAMA_PORT = 11434;
    private static final String DEFAULT_MODEL_NAME = "mistral";
    
    private final ChatLanguageModel chatModel;

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
            return chatModel.generate(question);
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur LangChain4J: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        LangChain4jClient client = new LangChain4jClient();
        String response = client.ask("Bonjour, peux-tu me parler de Java ?");
        System.out.println("Réponse: " + response);
    }
}
