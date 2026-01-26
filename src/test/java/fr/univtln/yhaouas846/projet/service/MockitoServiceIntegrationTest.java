package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.discord4j.services.DiscordBotService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

/**
 * Tests Quarkus + Mockito : vérifie le mapping HTTP de BotResource.
 */
@QuarkusTest
class MockitoServiceIntegrationTest {

    @InjectMock
    DiscordBotService discordBotService;

    @Test
    void createGuild_mapsIllegalArgumentTo400() {
        when(discordBotService.createGuildWithDefaultChannels(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Owner user not found"));

        given()
                .queryParam("name", "G")
                .queryParam("owner", "missing")
                .when().post("/api/bot/guilds")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("Owner user not found"));
    }

    @Test
    void sendMessage_mapsSecurityExceptionTo403() {
        when(discordBotService.sendMessage(anyLong(), anyLong(), anyString()))
                .thenThrow(new SecurityException("no permission"));

        given()
                .queryParam("authorId", 1)
                .queryParam("channelId", 2)
                .queryParam("content", "x")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("no permission"));
    }

    @Test
    void getChannelMessages_returnsOkFromService() {
        when(discordBotService.getChannelMessages(300L, 2)).thenReturn(List.of());

        given()
                .queryParam("limit", 2)
                .when().get("/api/bot/messages/channel/300")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }
}