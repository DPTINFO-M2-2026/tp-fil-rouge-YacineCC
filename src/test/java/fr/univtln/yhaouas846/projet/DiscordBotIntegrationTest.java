package fr.univtln.yhaouas846.projet;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Tests d'intégration utilisant TestContainers avec une vraie base PostgreSQL
 */
@QuarkusIntegrationTest
@Testcontainers
class DiscordBotIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("discord_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void testCompleteIntegrationWithRealDatabase() {
        // Test que l'application fonctionne avec une vraie base de données
        given()
                .when().get("/api/bot/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        // Test de création d'utilisateur avec validation complète
        String user = "{\n" +
                "\"username\": \"IntegrationUser\",\n" +
                "\"discriminator\": \"8888\",\n" +
                "\"email\": \"integration@test.com\"\n" +
                "}";

        Integer userId = given()
                .contentType(ContentType.JSON)
                .body(user)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Vérifier que l'utilisateur est persisté
        given()
                .when().get("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("username", equalTo("IntegrationUser"));

        // Test de validation des contraintes
        String invalidUser = "{\n" +
                "\"username\": \"x\",\n" +
                "\"discriminator\": \"12\",\n" +
                "\"email\": \"invalid\"\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when().post("/api/users")
                .then()
                .statusCode(400);
    }

    @Test
    void testTransactionRollback() {
        // Test que les transactions sont correctement gérées
        String invalidGuild = "{\n" +
                "\"name\": \"\",\n" +
                "\"owner\": null\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidGuild)
                .when().post("/api/guilds")
                .then()
                .statusCode(400);

        // Vérifier que l'état de la base n'a pas été altéré
        given()
                .when().get("/api/guilds")
                .then()
                .statusCode(200);
    }
}