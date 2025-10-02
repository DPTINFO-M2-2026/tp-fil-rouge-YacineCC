package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.service.DiscordBotService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test d'intégration avec Mockito pour démontrer l'utilisation des mocks
 * Note: Ces tests montrent les concepts Mockito mais ne remplacent pas les vrais services
 */
@ExtendWith(MockitoExtension.class)
class BotResourceMockTest {

    @Mock
    DiscordBotService discordBotService;

    @Test
    void testHealthCheckWithMockedService() {
        // Test simple qui ne dépend pas du service
        given()
                .when().get("/api/bot/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("UP"));
    }

    @Test
    void testCheckUserPermissionsWithMock() {
        // Mock du service pour retourner des valeurs prédictibles
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
        // Créer un message mock à retourner
        User mockAuthor = new User();
        mockAuthor.id = 1L;
        mockAuthor.username = "TestAuthor";

        fr.univtln.yhaouas846.projet.entity.Channel mockChannel = new fr.univtln.yhaouas846.projet.entity.Channel();
        mockChannel.id = 1L;
        mockChannel.name = "test-channel";

        fr.univtln.yhaouas846.projet.entity.Message mockMessage = new fr.univtln.yhaouas846.projet.entity.Message();
        mockMessage.id = 1L;
        mockMessage.content = "Test message";
        mockMessage.author = mockAuthor;
        mockMessage.channel = mockChannel;

        // Mock du service pour simuler un envoi réussi
        when(discordBotService.sendMessage(eq(1L), eq(1L), eq("Test message")))
                .thenReturn(mockMessage);

        // Test de l'endpoint
        given()
                .queryParam("authorId", 1)
                .queryParam("channelId", 1)
                .queryParam("content", "Test message")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("content", equalTo("Test message"))
                .body("author.id", equalTo(1));

        // Vérifier que le service a été appelé avec les bons paramètres
        verify(discordBotService).sendMessage(1L, 1L, "Test message");
    }

    @Test
    void testSendMessageWithSecurityException() {
        // Mock du service pour simuler une exception de sécurité
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

    @Test
    void testAddUserToGuildWithMock() {
        // Mock du service pour ne rien faire (void method)
        doNothing().when(discordBotService).addUserToGuild(anyLong(), anyLong());

        // Test de l'endpoint
        given()
                .when().post("/api/bot/guilds/1/members/2")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));

        // Vérifier que le service a été appelé
        verify(discordBotService).addUserToGuild(2L, 1L);
    }

    @Test
    void testAddUserToGuildWithException() {
        // Mock du service pour simuler une exception
        doThrow(new IllegalArgumentException("User not found"))
                .when(discordBotService).addUserToGuild(anyLong(), anyLong());

        // Test de l'endpoint avec exception
        given()
                .when().post("/api/bot/guilds/999/members/999")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("User not found"));

        // Vérifier que le service a été appelé
        verify(discordBotService).addUserToGuild(999L, 999L);
    }

    @Test
    void testDeleteMessageWithMock() {
        // Mock du service pour ne rien faire (void method)
        doNothing().when(discordBotService).deleteMessage(anyLong(), anyLong());

        // Test de l'endpoint
        given()
                .queryParam("requesterId", 1)
                .when().delete("/api/bot/messages/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));

        // Vérifier que le service a été appelé
        verify(discordBotService).deleteMessage(1L, 1L);
    }

    @Test
    void testDeleteMessageWithSecurityException() {
        // Mock du service pour simuler une exception de sécurité
        doThrow(new SecurityException("User doesn't have permission to delete"))
                .when(discordBotService).deleteMessage(anyLong(), anyLong());

        // Test de l'endpoint avec exception de sécurité
        given()
                .queryParam("requesterId", 2)
                .when().delete("/api/bot/messages/1")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));

        // Vérifier que le service a été appelé
        verify(discordBotService).deleteMessage(1L, 2L);
    }

    @Test
    void testMockitoVerifications() {
        // Test pour démontrer différentes façons de vérifier avec Mockito
        
        // Reset le mock pour ce test
        Mockito.reset(discordBotService);

        // Configurer le mock
        when(discordBotService.isUserBotOwner(anyLong())).thenReturn(false);

        // Faire plusieurs appels
        given().when().get("/api/bot/users/1/permissions").then().statusCode(200);
        given().when().get("/api/bot/users/2/permissions").then().statusCode(200);
        given().when().get("/api/bot/users/3/permissions").then().statusCode(200);

        // Vérifications diverses avec Mockito
        verify(discordBotService, times(3)).isUserBotOwner(anyLong());
        verify(discordBotService, atLeastOnce()).isUserBotOwner(1L);
        verify(discordBotService, atLeastOnce()).isUserBotOwner(2L);
        verify(discordBotService, atLeastOnce()).isUserBotOwner(3L);

        // Vérifier qu'aucune autre méthode n'a été appelée
        verifyNoMoreInteractions(discordBotService);
    }

    @Test
    void testMockitoArgumentCapture() {
        // Test pour démontrer la capture d'arguments avec Mockito
        
        when(discordBotService.isUserBotOwner(anyLong())).thenReturn(true);

        // Faire un appel
        given().when().get("/api/bot/users/42/permissions").then().statusCode(200);

        // Capturer et vérifier l'argument
        verify(discordBotService).isUserBotOwner(eq(42L));
    }
}