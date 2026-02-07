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
}