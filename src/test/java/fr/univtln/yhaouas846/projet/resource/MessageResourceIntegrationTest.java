package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MessageResourceIntegrationTest {

    @Test
    void createMessage_upsertsOnDiscordId() {
        String first = "{\n" +
                "\"discordId\": \"discord-xyz\",\n" +
                "\"content\": \"First\",\n" +
                "\"author\": {\"id\": 100},\n" +
                "\"channel\": {\"id\": 300}\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(first)
                .when().post("/api/messages")
                .then()
                .statusCode(201)
                .body("discordId", equalTo("discord-xyz"))
                .body("content", equalTo("First"));

        String second = "{\n" +
                "\"discordId\": \"discord-xyz\",\n" +
                "\"content\": \"Second\",\n" +
                "\"isEdited\": true,\n" +
                "\"author\": {\"id\": 100},\n" +
                "\"channel\": {\"id\": 300}\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(second)
                .when().post("/api/messages")
                .then()
                .statusCode(200)
                .body("discordId", equalTo("discord-xyz"))
                .body("content", equalTo("Second"));
    }

    @Test
    void searchMessages_returnsEmptyWhenQueryBlank() {
        given()
                .queryParam("content", "   ")
                .when().get("/api/messages/search")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void deleteMessage_isSoftDelete() {
        // message 500 existe en seed et n'est pas supprimé
        given()
                .when().delete("/api/messages/500")
                .then()
                .statusCode(204);

        given()
                .when().get("/api/messages/500")
                .then()
                .statusCode(200)
                .body("isDeleted", equalTo(true));
    }
}
