package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
class BotResourceTest {

    @Test
    void testHealthCheck() {
        given()
                .when().get("/api/bot/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("UP"))
                .body("service", equalTo("Discord Bot API"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testCreateGuildWithDefaults() {
        given()
                .queryParam("name", "Bot Test Guild")
                .queryParam("owner", "TestUser1")
                .when().post("/api/bot/guilds")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Bot Test Guild"))
                .body("owner.username", equalTo("TestUser1"));
    }

    @Test
    void testCreateGuildWithInvalidOwner() {
        given()
                .queryParam("name", "Invalid Guild")
                .queryParam("owner", "NonExistentUser")
                .when().post("/api/bot/guilds")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("Owner user not found"));
    }

    @Test
    void testSendMessageAllowedBySeededPermissions() {
        given()
                .queryParam("authorId", 100)
                .queryParam("channelId", 300)
                .queryParam("content", "Test message from bot API")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("author.id", equalTo(100))
                .body("channel.id", equalTo(300));
    }

    @Test
    void testSendMessageInvalidAuthor() {
        given()
                .queryParam("authorId", 99999)
                .queryParam("channelId", 300)
                .queryParam("content", "Test message")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("not found"));
    }

    @Test
    void testSendMessageForbiddenWhenNotMember() {
        // User 103 n'est pas membre de la guilde du channel 300
        given()
                .queryParam("authorId", 103)
                .queryParam("channelId", 300)
                .queryParam("content", "should be forbidden")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));
    }

    @Test
    void testGetChannelMessages() {
        given()
                .queryParam("limit", 5)
                .when().get("/api/bot/messages/channel/300")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    void testGetChannelMessagesInvalidChannel() {
        given()
                .when().get("/api/bot/messages/channel/99999")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("not found"));
    }

    @Test
    void testAddUserToGuildInvalidIds() {
        given()
                .when().post("/api/bot/guilds/99999/members/100")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("not found"));
    }

    @Test
    void testGetUserGuilds() {
        given()
                .when().get("/api/bot/users/100/guilds")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    void testGetUserGuildsInvalidUser() {
        given()
                .when().get("/api/bot/users/99999/guilds")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("not found"));
    }

    @Test
    void testCheckUserPermissions() {
        given()
                .when().get("/api/bot/users/100/permissions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(100))
                .body("isBotOwner", notNullValue());
    }

    @Test
    void testDeleteMessage() {
        given()
                .queryParam("requesterId", 100)
                .when().delete("/api/bot/messages/500")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));
    }

    @Test
    void testDeleteMessageInvalidId() {
        given()
                .queryParam("requesterId", 100)
                .when().delete("/api/bot/messages/99999")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", containsString("not found"));
    }

    /**
     * Vérifie que la suppression d'un message par un utilisateur sans permission retourne 403.
     * User 103 est membre de guild 201 mais PAS de guild 200 (où se trouve le message 501).
     */
    @Test
    void testDeleteMessageForbidden() {
        given()
                .queryParam("requesterId", 103)
                .when().delete("/api/bot/messages/501")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("error", containsString("permission"));
    }

    /**
     * Vérifie qu'un modérateur (rôle canManageMessages) peut supprimer le message d'un autre utilisateur.
     * User 100 a le rôle 400 (Test Admin, canManageMessages=true) dans guild 200.
     * Message 501 est de user 101 dans channel 300 (guild 200).
     */
    @Test
    void testDeleteMessageByModerator() {
        given()
                .queryParam("requesterId", 100)
                .when().delete("/api/bot/messages/501")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));
    }

    /**
     * Vérifie l'ajout d'un membre à une guilde avec des IDs valides.
     * User 103 n'est pas membre de guild 200 → on l'ajoute.
     */
    @Test
    void testAddUserToGuildSuccess() {
        given()
                .when().post("/api/bot/guilds/200/members/103")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", containsString("successfully"));
    }
}