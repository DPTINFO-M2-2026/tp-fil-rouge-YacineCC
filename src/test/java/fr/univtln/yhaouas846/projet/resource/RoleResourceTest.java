package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests d'intégration de {@link RoleResource}.
 *
 * <p>Données de test ({@code test-data.sql}) :</p>
 * <ul>
 *   <li>400 – Test Admin  (guild 200, toutes permissions)</li>
 *   <li>401 – Test Member (guild 200, send + read uniquement)</li>
 *   <li>402 – Test Moderator (guild 201)</li>
 * </ul>
 */
@QuarkusTest
class RoleResourceTest {

    // ── GET /api/roles ───────────────────────────────────────────────────

    @Test
    void testGetAllRoles() {
        given()
                .when().get("/api/roles")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    // ── GET /api/roles/{id} ──────────────────────────────────────────────

    @Test
    void testGetRoleById() {
        given()
                .when().get("/api/roles/400")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test Admin"))
                .body("color", equalTo("#FF0000"))
                .body("canManageChannels", equalTo(true));
    }

    @Test
    void testGetRoleById_notFound() {
        given()
                .when().get("/api/roles/99999")
                .then()
                .statusCode(404);
    }

    // ── POST /api/roles ──────────────────────────────────────────────────

    @Test
    void testCreateAndDeleteRole() {
        String newRole = "{\n" +
                "\"name\": \"TestNewRole\",\n" +
                "\"color\": \"#ABCDEF\",\n" +
                "\"guild\": {\"id\": 200},\n" +
                "\"position\": 5,\n" +
                "\"canSendMessages\": true,\n" +
                "\"canReadMessages\": true\n" +
                "}";

        Integer roleId = given()
                .contentType(ContentType.JSON)
                .body(newRole)
                .when().post("/api/roles")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("TestNewRole"))
                .body("color", equalTo("#ABCDEF"))
                .extract().path("id");

        // Cleanup
        given().when().delete("/api/roles/" + roleId).then().statusCode(204);
    }

    // ── PUT /api/roles/{id} ──────────────────────────────────────────────

    @Test
    void testUpdateRole() {
        // Créer un rôle à modifier
        String role = "{\n" +
                "\"name\": \"RoleToUpdate\",\n" +
                "\"color\": \"#111111\",\n" +
                "\"guild\": {\"id\": 200}\n" +
                "}";

        Integer roleId = given()
                .contentType(ContentType.JSON)
                .body(role)
                .when().post("/api/roles")
                .then()
                .statusCode(201)
                .extract().path("id");

        String updated = "{\n" +
                "\"name\": \"RoleUpdated\",\n" +
                "\"color\": \"#222222\",\n" +
                "\"guild\": {\"id\": 200},\n" +
                "\"canKickMembers\": true\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when().put("/api/roles/" + roleId)
                .then()
                .statusCode(200)
                .body("name", equalTo("RoleUpdated"))
                .body("color", equalTo("#222222"))
                .body("canKickMembers", equalTo(true));

        // Cleanup
        given().when().delete("/api/roles/" + roleId).then().statusCode(204);
    }

    @Test
    void testUpdateRole_notFound() {
        String payload = "{\n" +
                "\"name\": \"Nope\",\n" +
                "\"color\": \"#000000\",\n" +
                "\"guild\": {\"id\": 200}\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().put("/api/roles/99999")
                .then()
                .statusCode(404);
    }

    // ── DELETE /api/roles/{id} ───────────────────────────────────────────

    @Test
    void testDeleteRole_notFound() {
        given()
                .when().delete("/api/roles/99999")
                .then()
                .statusCode(404);
    }

    // ── GET /api/roles/guild/{guildId} ───────────────────────────────────

    @Test
    void testGetRolesByGuild() {
        given()
                .when().get("/api/roles/guild/200")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    void testGetRolesByGuild_notFound() {
        given()
                .when().get("/api/roles/guild/99999")
                .then()
                .statusCode(404);
    }

    // ── POST + DELETE /api/roles/{roleId}/users/{userId} ─────────────────

    @Test
    void testAssignAndRemoveRole_roundTrip() {
        // user 103 n'a pas le rôle 400 → on l'assigne puis on le retire
        given()
                .contentType(ContentType.JSON)
                .when().post("/api/roles/400/users/103")
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when().delete("/api/roles/400/users/103")
                .then()
                .statusCode(200);
    }

    @Test
    void testAssignRole_roleNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/api/roles/99999/users/100")
                .then()
                .statusCode(404);
    }

    @Test
    void testAssignRole_userNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/api/roles/400/users/99999")
                .then()
                .statusCode(404);
    }
}
