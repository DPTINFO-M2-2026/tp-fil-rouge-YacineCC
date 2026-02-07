package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.discord4j.services.DiscordBotService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

/**
 * Test Quarkus + Mockito: BotResource avec service mocké.
 */
@QuarkusTest
class BotResourceMockTest {

        @InjectMock
        DiscordBotService discordBotService;

    @Test
    void testCheckUserPermissionsWithMock() {
        when(discordBotService.isUserBotOwner(1L)).thenReturn(true);
        when(discordBotService.isUserBotOwner(2L)).thenReturn(false);

        // Test avec un admin
        given()
                .when().get("/api/bot/users/1/permissions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(1))
                .body("isBotOwner", equalTo(true));

        // Test avec un utilisateur normal
        given()
                .when().get("/api/bot/users/2/permissions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(2))
                .body("isBotOwner", equalTo(false));

        // Vérifier que le service a été appelé
        verify(discordBotService).isUserBotOwner(1L);
        verify(discordBotService).isUserBotOwner(2L);
    }

    @Test
    void testSendMessageWithMockedService() {
        fr.univtln.yhaouas846.projet.entity.Message mockMessage = new fr.univtln.yhaouas846.projet.entity.Message();
        mockMessage.id = 1L;
        mockMessage.content = "Test message";

        when(discordBotService.sendMessage(eq(1L), eq(1L), eq("Test message"))).thenReturn(mockMessage);

        given()
                .queryParam("authorId", 1)
                .queryParam("channelId", 1)
                .queryParam("content", "Test message")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("content", equalTo("Test message"));

        // Vérifier que le service a été appelé avec les bons paramètres
        verify(discordBotService).sendMessage(1L, 1L, "Test message");
    }

    @Test
    void testSendMessageWithSecurityException() {
        when(discordBotService.sendMessage(anyLong(), anyLong(), anyString()))
                .thenThrow(new SecurityException("User doesn't have permission"));

        // Test de l'endpoint avec exception
        given()
                .queryParam("authorId", 1)
                .queryParam("channelId", 1)
                .queryParam("content", "Unauthorized message")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));

        // Vérifier que le service a été appelé
        verify(discordBotService).sendMessage(1L, 1L, "Unauthorized message");
    }

    /**
     * Vérifie que DELETE /api/bot/messages/{id} retourne 403 quand le service lève SecurityException.
     */
    @Test
    void testDeleteMessageForbiddenWithMock() {
        doThrow(new SecurityException("User doesn't have permission to delete this message"))
                .when(discordBotService).deleteMessage(eq(500L), eq(999L));

        given()
                .queryParam("requesterId", 999)
                .when().delete("/api/bot/messages/500")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));

        verify(discordBotService).deleteMessage(500L, 999L);
    }

    /**
     * Vérifie que DELETE /api/bot/messages/{id} retourne 200 en cas de succès.
     */
    @Test
    void testDeleteMessageSuccessWithMock() {
        doNothing().when(discordBotService).deleteMessage(eq(500L), eq(100L));

        given()
                .queryParam("requesterId", 100)
                .when().delete("/api/bot/messages/500")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));

        verify(discordBotService).deleteMessage(500L, 100L);
    }

    /**
     * Vérifie que POST /api/bot/guilds/{guildId}/members/{userId} délègue au service.
     */
    @Test
    void testAddUserToGuildWithMock() {
        doNothing().when(discordBotService).addUserToGuild(eq(50L), eq(200L));

        given()
                .when().post("/api/bot/guilds/200/members/50")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));

        verify(discordBotService).addUserToGuild(50L, 200L);
    }

    /**
     * Vérifie que POST /api/bot/guilds crée une guilde via le service mocké.
     */
    @Test
    void testCreateGuildWithMock() {
        fr.univtln.yhaouas846.projet.entity.Guild mockGuild = new fr.univtln.yhaouas846.projet.entity.Guild();
        mockGuild.id = 999L;
        mockGuild.name = "Mock Guild";

        when(discordBotService.createGuildWithDefaultChannels(eq("Mock Guild"), eq("owner")))
                .thenReturn(mockGuild);

        given()
                .queryParam("name", "Mock Guild")
                .queryParam("owner", "owner")
                .when().post("/api/bot/guilds")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Mock Guild"));

        verify(discordBotService).createGuildWithDefaultChannels("Mock Guild", "owner");
    }
}