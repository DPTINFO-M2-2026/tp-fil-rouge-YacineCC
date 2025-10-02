package fr.univtln.yhaouas846.projet;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests fonctionnels complets simulant un scénario d'utilisation réel du bot Discord
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class DiscordBotFunctionalTest {

    private static Long createdUserId;
    private static Long createdGuildId;
    private static Long createdChannelId;
    private static Long createdRoleId;
    private static Long createdMessageId;

    @Test
    @Order(1)
    void testCompleteUserCreationFlow() {
        // Créer un nouvel utilisateur
        String newUser = "{\n" +
                "\"username\": \"FunctionalTestUser\",\n" +
                "\"discriminator\": \"7777\",\n" +
                "\"email\": \"functional@test.com\",\n" +
                "\"isBot\": false\n" +
                "}";

        createdUserId = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("username", equalTo("FunctionalTestUser"))
                .body("discriminator", equalTo("7777"))
                .body("email", equalTo("functional@test.com"))
                .body("isBot", equalTo(false))
                .extract().path("id");

        // Vérifier que l'utilisateur peut être récupéré
        given()
                .when().get("/api/users/" + createdUserId)
                .then()
                .statusCode(200)
                .body("username", equalTo("FunctionalTestUser"));
    }

    @Test
    @Order(2)
    void testCompleteGuildCreationWithBotService() {
        // Utiliser le service bot pour créer une guilde avec canaux et rôles par défaut
        createdGuildId = given()
                .queryParam("name", "Functional Test Guild")
                .queryParam("owner", "FunctionalTestUser")
                .when().post("/api/bot/guilds")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Functional Test Guild"))
                .body("owner.username", equalTo("FunctionalTestUser"))
                .extract().path("id");

        // Vérifier que les canaux par défaut ont été créés
        given()
                .when().get("/api/channels/guild/" + createdGuildId)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(2)); // Au moins general, announcements, voice

        // Vérifier que les rôles par défaut ont été créés
        given()
                .when().get("/api/roles/guild/" + createdGuildId)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(1)); // Au moins Admin et Member
    }

    @Test
    @Order(3)
    void testAddUserToGuildAndAssignRole() {
        // Ajouter l'utilisateur à sa propre guilde via le service bot
        given()
                .when().post("/api/bot/guilds/" + createdGuildId + "/members/" + createdUserId)
                .then()
                .statusCode(200);

        // Vérifier que l'utilisateur est maintenant membre de la guilde
        given()
                .when().get("/api/bot/users/" + createdUserId + "/guilds")
                .then()
                .statusCode(200)
                .body("find { it.id == " + createdGuildId + " }", notNullValue());
    }

    @Test
    @Order(4)
    void testCreateChannelInGuild() {
        String newChannel = "{\n" +
                "\"name\": \"functional-test-channel\",\n" +
                "\"description\": \"Channel for functional tests\",\n" +
                "\"type\": \"TEXT\",\n" +
                "\"guild\": {\"id\": " + createdGuildId + "},\n" +
                "\"position\": 10\n" +
                "}";

        createdChannelId = given()
                .contentType(ContentType.JSON)
                .body(newChannel)
                .when().post("/api/channels")
                .then()
                .statusCode(201)
                .body("name", equalTo("functional-test-channel"))
                .body("guild.id", equalTo(createdGuildId.intValue()))
                .extract().path("id");
    }

    @Test
    @Order(5)
    void testCreateRoleWithPermissions() {
        String newRole = "{\n" +
                "\"name\": \"Functional Tester\",\n" +
                "\"color\": \"#00FF00\",\n" +
                "\"guild\": {\"id\": " + createdGuildId + "},\n" +
                "\"position\": 5,\n" +
                "\"canSendMessages\": true,\n" +
                "\"canReadMessages\": true,\n" +
                "\"canManageMessages\": false\n" +
                "}";

        createdRoleId = given()
                .contentType(ContentType.JSON)
                .body(newRole)
                .when().post("/api/roles")
                .then()
                .statusCode(201)
                .body("name", equalTo("Functional Tester"))
                .body("color", equalTo("#00FF00"))
                .extract().path("id");

        // Assigner le rôle à l'utilisateur
        given()
                .when().post("/api/roles/" + createdRoleId + "/users/" + createdUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    void testSendMessageWithPermissions() {
        // L'utilisateur devrait maintenant pouvoir envoyer des messages
        createdMessageId = given()
                .queryParam("authorId", createdUserId)
                .queryParam("channelId", createdChannelId)
                .queryParam("content", "Hello from functional test!")
                .when().post("/api/bot/messages")
                .then()
                .statusCode(201)
                .body("content", equalTo("Hello from functional test!"))
                .body("author.id", equalTo(createdUserId.intValue()))
                .body("channel.id", equalTo(createdChannelId.intValue()))
                .extract().path("id");

        // Vérifier que le message apparaît dans les messages du canal
        given()
                .when().get("/api/bot/messages/channel/" + createdChannelId)
                .then()
                .statusCode(200)
                .body("find { it.id == " + createdMessageId + " }.content", 
                      equalTo("Hello from functional test!"));
    }

    @Test
    @Order(7)
    void testMessageManagement() {
        // Mettre à jour le message
        String updatedMessage = "{\n" +
                "\"content\": \"Updated functional test message\",\n" +
                "\"author\": {\"id\": " + createdUserId + "},\n" +
                "\"channel\": {\"id\": " + createdChannelId + "}\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(updatedMessage)
                .when().put("/api/messages/" + createdMessageId)
                .then()
                .statusCode(200)
                .body("content", equalTo("Updated functional test message"))
                .body("isEdited", equalTo(true));
    }

    @Test
    @Order(8)
    void testSearchFunctionality() {
        // Rechercher des messages par contenu
        given()
                .queryParam("content", "functional test")
                .queryParam("limit", 10)
                .when().get("/api/messages/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("find { it.content.contains('functional test') }", notNullValue());

        // Récupérer les messages d'un utilisateur
        given()
                .queryParam("limit", 10)
                .when().get("/api/messages/user/" + createdUserId)
                .then()
                .statusCode(200)
                .body("find { it.author.id == " + createdUserId.intValue() + " }", notNullValue());
    }

    @Test
    @Order(9)
    void testPermissionsAndSecurity() {
        // Vérifier les permissions de l'utilisateur
        given()
                .when().get("/api/bot/users/" + createdUserId + "/permissions")
                .then()
                .statusCode(200)
                .body("userId", equalTo(createdUserId.intValue()))
                .body("isBotOwner", equalTo(false));

        // Tenter de supprimer un message (devrait réussir car c'est l'auteur)
        given()
                .queryParam("requesterId", createdUserId)
                .when().delete("/api/bot/messages/" + createdMessageId)
                .then()
                .statusCode(200);

        // Vérifier que le message est marqué comme supprimé
        given()
                .when().get("/api/messages/" + createdMessageId)
                .then()
                .statusCode(200)
                .body("isDeleted", equalTo(true));
    }

    @Test
    @Order(10)
    void testDataConsistency() {
        // Vérifier l'intégrité des données après toutes les opérations
        
        // L'utilisateur devrait toujours exister
        given()
                .when().get("/api/users/" + createdUserId)
                .then()
                .statusCode(200);

        // La guilde devrait avoir les bonnes associations
        given()
                .when().get("/api/guilds/" + createdGuildId + "/members")
                .then()
                .statusCode(200)
                .body("find { it.id == " + createdUserId.intValue() + " }", notNullValue());

        // Le canal devrait appartenir à la bonne guilde
        given()
                .when().get("/api/channels/" + createdChannelId)
                .then()
                .statusCode(200)
                .body("guild.id", equalTo(createdGuildId.intValue()));

        // Le rôle devrait être assigné à l'utilisateur
        given()
                .when().get("/api/roles/" + createdRoleId)
                .then()
                .statusCode(200)
                .body("users.find { it.id == " + createdUserId.intValue() + " }", notNullValue());
    }

    @Test
    @Order(11)
    void testHealthAndMonitoring() {
        // Test de santé général
        given()
                .when().get("/api/bot/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        // Vérifier les statistiques générales
        given()
                .when().get("/api/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .when().get("/api/guilds")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .when().get("/api/channels")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(12)
    void testCleanup() {
        // Nettoyer les données de test créées
        // (En production, ceci serait géré par les transactions de test)
        
        // Supprimer le rôle de l'utilisateur
        given()
                .when().delete("/api/roles/" + createdRoleId + "/users/" + createdUserId)
                .then()
                .statusCode(200);

        // Retirer l'utilisateur de la guilde
        given()
                .when().delete("/api/guilds/" + createdGuildId + "/members/" + createdUserId)
                .then()
                .statusCode(200);

        // Note: En conditions réelles, la suppression en cascade serait gérée par la base de données
    }
}