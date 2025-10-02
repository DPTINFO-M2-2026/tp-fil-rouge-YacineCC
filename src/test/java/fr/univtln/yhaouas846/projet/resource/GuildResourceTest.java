package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class GuildResourceTest {

    @Test
    @Order(1)
    void testGetAllGuilds() {
        given()
                .when().get("/api/guilds")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    void testGetGuildById() {
        given()
                .when().get("/api/guilds/200")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test Guild 1"))
                .body("owner.username", equalTo("TestUser1"));
    }

    @Test
    @Order(3)
    void testCreateGuild() {
        String newGuild = "{\n" +
                "\"name\": \"New API Guild\",\n" +
                "\"description\": \"Guild created via API\",\n" +
                "\"owner\": {\"id\": 100},\n" +
                "\"memberLimit\": 150\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(newGuild)
                .when().post("/api/guilds")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("New API Guild"));
    }

    @Test
    @Order(4)
    void testGetGuildMembers() {
        given()
                .when().get("/api/guilds/200/members")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(-1));
    }

    @Test
    @Order(5)
    void testAddMemberToGuild() {
        given()
                .when().post("/api/guilds/200/members/103")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    void testRemoveMemberFromGuild() {
        given()
                .when().delete("/api/guilds/200/members/103")
                .then()
                .statusCode(200);
    }
}