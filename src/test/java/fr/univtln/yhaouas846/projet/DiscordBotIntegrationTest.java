package fr.univtln.yhaouas846.projet;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test d'intégration Quarkus qui démarre l'application avec le profil test
 * et la base H2 (configurée via src/test/resources/application.properties).
 */
@QuarkusTest
class DiscordBotIntegrationTest {

    @Test
    void applicationStarts_andHealthIsUp() {
        given()
                .when().get("/api/bot/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void usesH2SeedData_andBusinessRulesAreEnforced() {
        // Données seedées via test-data.sql
        given()
                .when().get("/api/users/100")
                .then()
                .statusCode(200)
                .body("username", equalTo("TestUser1"));

        // User 103 n'est pas membre de la guilde du channel 300 => doit être refusé
        given()
                .queryParam("authorId", 103)
                .queryParam("channelId", 300)
                .queryParam("content", "should be forbidden")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));

        // User 100 est membre et a un rôle avec canSendMessages => OK
        given()
                .queryParam("authorId", 100)
                .queryParam("channelId", 300)
                .queryParam("content", "hello")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("content", equalTo("hello"))
                .body("author.id", equalTo(100))
                .body("channel.id", equalTo(300));
    }
}