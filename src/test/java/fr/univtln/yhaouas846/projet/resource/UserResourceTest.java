package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
class UserResourceTest {

    @Test
    void testGetAllUsers() {
        given()
                .when().get("/api/users")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    void testGetUserById() {
        given()
                .when().get("/api/users/100")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo("TestUser1"))
                .body("discriminator", equalTo("1111"));
    }

    @Test
    void testGetUserByIdNotFound() {
        given()
                .when().get("/api/users/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateUser() {
        String newUser = "{\n" +
                "\"username\": \"NewTestUser\",\n" +
                "\"discriminator\": \"9876\",\n" +
                "\"email\": \"newuser@test.com\",\n" +
                "\"isBot\": false\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("username", equalTo("NewTestUser"))
                .body("discriminator", equalTo("9876"))
                .body("email", equalTo("newuser@test.com"));
    }

    @Test
    void testCreateUserInvalidData() {
        String invalidUser = "{\n" +
                "\"username\": \"a\",\n" +
                "\"discriminator\": \"123\",\n" +
                "\"email\": \"invalid-email\"\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when().post("/api/users")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetUserByUsername() {
        given()
                .when().get("/api/users/username/TestUser1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo("TestUser1"));
    }

    @Test
    void testGetBots() {
        given()
                .when().get("/api/users/bots")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("findAll { it.isBot == true }.size()", greaterThan(0));
    }

    @Test
    void testDeleteUser() {
        // Créer d'abord un utilisateur à supprimer
        String userToDelete = "{\n" +
                "\"username\": \"ToDelete\",\n" +
                "\"discriminator\": \"0000\",\n" +
                "\"email\": \"delete@test.com\"\n" +
                "}";

        Integer userId = given()
                .contentType(ContentType.JSON)
                .body(userToDelete)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Supprimer l'utilisateur
        given()
                .when().delete("/api/users/" + userId)
                .then()
                .statusCode(204);

        // Vérifier que l'utilisateur n'existe plus
        given()
                .when().get("/api/users/" + userId)
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateUser_roundTripOnCreatedUser() {
        String newUser = "{\n" +
                "\"username\": \"ToUpdate\",\n" +
                "\"discriminator\": \"3333\",\n" +
                "\"email\": \"toupdate@test.com\",\n" +
                "\"isBot\": false\n" +
                "}";

        Integer userId = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id");

        String updatedUser = "{\n" +
                "\"username\": \"UpdatedUser\",\n" +
                "\"discriminator\": \"5555\",\n" +
                "\"email\": \"updated@test.com\",\n" +
                "\"isBot\": false\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(updatedUser)
                .when().put("/api/users/" + userId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo("UpdatedUser"));

        given().when().delete("/api/users/" + userId).then().statusCode(204);
    }
}