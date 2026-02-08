package fr.univtln.yhaouas846.projet.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Resource pour la page d'accueil de l'API.
 */
@Path("/")
public class IndexResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        return Response.ok(Map.of(
            "name", "Discord Bot API",
            "version", "1.0.0",
            "description", "API REST pour le bot Discord Thrasher avec scan automatique et IA",
            "status", "running",
            "endpoints", Map.of(
                "users", "/api/users",
                "guilds", "/api/guilds",
                "channels", "/api/channels",
                "messages", "/api/messages",
                "roles", "/api/roles",
                "bot", "/api/bot"
            ),
            "features", new String[]{
                "Automatic guild scanning every 60 seconds",
                "AI-powered commands (LLM via Ollama)",
                "Reactive architecture (Discord4j + Reactor)",
                "PostgreSQL persistence"
            }
        )).build();
    }
}
