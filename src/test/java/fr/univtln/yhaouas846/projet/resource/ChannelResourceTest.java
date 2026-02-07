package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests d'intégration de {@link ChannelResource}.
 *
 * <p>Les données de test proviennent de {@code test-data.sql} :
 * canaux 300 (test-general, TEXT, guild 200), 301 (test-voice, VOICE, guild 200),
 * 302 (test-announcements, TEXT, guild 201).</p>
 */
@QuarkusTest
class ChannelResourceTest {

    // ── GET /api/channels ────────────────────────────────────────────────

    @Test
    void testGetAllChannels() {
        given()
                .when().get("/api/channels")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    // ── GET /api/channels/{id} ───────────────────────────────────────────

    @Test
    void testGetChannelById() {
        given()
                .when().get("/api/channels/300")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("test-general"))
                .body("type", equalTo("TEXT"));
    }

    @Test
    void testGetChannelById_notFound() {
        given()
                .when().get("/api/channels/99999")
                .then()
                .statusCode(404);
    }

    // ── POST /api/channels ───────────────────────────────────────────────

    @Test
    void testCreateAndDeleteChannel() {
        String newChannel = "{\n" +
                "\"name\": \"test-new-channel\",\n" +
                "\"type\": \"TEXT\",\n" +
                "\"guild\": {\"id\": 200},\n" +
                "\"position\": 5\n" +
                "}";

        Integer channelId = given()
                .contentType(ContentType.JSON)
                .body(newChannel)
                .when().post("/api/channels")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("test-new-channel"))
                .body("type", equalTo("TEXT"))
                .extract().path("id");

        // Cleanup
        given().when().delete("/api/channels/" + channelId).then().statusCode(204);
    }

    // ── PUT /api/channels/{id} ───────────────────────────────────────────

    @Test
    void testUpdateChannel() {
        // Créer un canal à modifier
        String channel = "{\n" +
                "\"name\": \"channel-to-update\",\n" +
                "\"type\": \"TEXT\",\n" +
                "\"guild\": {\"id\": 200}\n" +
                "}";

        Integer channelId = given()
                .contentType(ContentType.JSON)
                .body(channel)
                .when().post("/api/channels")
                .then()
                .statusCode(201)
                .extract().path("id");

        String updated = "{\n" +
                "\"name\": \"channel-updated\",\n" +
                "\"type\": \"VOICE\",\n" +
                "\"guild\": {\"id\": 200},\n" +
                "\"position\": 10\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when().put("/api/channels/" + channelId)
                .then()
                .statusCode(200)
                .body("name", equalTo("channel-updated"))
                .body("type", equalTo("VOICE"));

        // Cleanup
        given().when().delete("/api/channels/" + channelId).then().statusCode(204);
    }

    @Test
    void testUpdateChannel_notFound() {
        String payload = "{\n" +
                "\"name\": \"nope\",\n" +
                "\"type\": \"TEXT\",\n" +
                "\"guild\": {\"id\": 200}\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/channels/99999")
                .then()
                .statusCode(404);
    }

    // ── DELETE /api/channels/{id} ────────────────────────────────────────

    @Test
    void testDeleteChannel_notFound() {
        given()
                .when().delete("/api/channels/99999")
                .then()
                .statusCode(404);
    }

    // ── GET /api/channels/guild/{guildId} ────────────────────────────────

    @Test
    void testGetChannelsByGuild() {
        given()
                .when().get("/api/channels/guild/200")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("name", hasItems("test-general", "test-voice"));
    }

    @Test
    void testGetChannelsByGuild_notFound() {
        given()
                .when().get("/api/channels/guild/99999")
                .then()
                .statusCode(404);
    }

    // ── GET /api/channels/type/{type} ────────────────────────────────────

    @Test
    void testGetChannelsByType() {
        given()
                .when().get("/api/channels/type/TEXT")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }
}
