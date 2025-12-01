package fr.univtln.yhaouas846.discord4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MistralClient {
    private static final String API_URL = "https://api.mistral.ai/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;

    public MistralClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String ask(String userMessage) throws IOException, InterruptedException {
        String body = "{" +
                "\"model\": \"mistral-small-latest\"," +
                "\"messages\": [{\"role\": \"user\", \"content\": " + escapeJson(userMessage) + "}]" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String escapeJson(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
