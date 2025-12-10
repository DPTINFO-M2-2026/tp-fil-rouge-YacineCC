package fr.univtln.yhaouas846.discord4j.services;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatModel;

import java.time.Duration;

public class OllamaClient {
    private final ChatModel chatModel;

    public OllamaClient(String host, int port, String modelName) {
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://" + host + ":" + port)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        System.out.println("Ollama connecté à http://" + host + ":" + port + " (modèle: " + modelName + ")");
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


    // Exemple rapide pour tester localement
    public static void main(String[] args) {
        OllamaClient client = new OllamaClient("192.168.56.1", 11434, "llama3.2:1b");

        String response = client.ask("Bonjour, peux-tu me parler de Java ?");
        System.out.println("Réponse ask: " + response);

        String translation = client.translate("Hello, how are you?");
        System.out.println("Traduction: " + translation);
    }
}
