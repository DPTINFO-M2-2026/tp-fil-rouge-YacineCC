package fr.univtln.yhaouas846.projet.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
class GuildResourceTest {

    @Test
    void testGetAllGuilds() {
        given()
                .when().get("/api/guilds")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
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
    void testCreateGuild() {
        String newGuild = "{\n" +
                "\"name\": \"New API Guild\",\n" +
                "\"description\": \"Guild created via API\",\n" +
                "\"owner\": {\"id\": 100},\n" +
                "\"memberLimit\": 150\n" +
                "}";

        Integer guildId = given()
            .contentType(ContentType.JSON)
            .body(newGuild)
            .when().post("/api/guilds")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("name", equalTo("New API Guild"))
            .extract().path("id");

        // Cleanup
        given().when().delete("/api/guilds/" + guildId).then().statusCode(204);
    }

    @Test
    void testGetGuildMembers() {
        given()
                .when().get("/api/guilds/200/members")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(-1));
    }

    @Test
        void testAddAndRemoveMemberToGuild_roundTrip() {
        // Add user 103 to guild 200
        given()
            .when().post("/api/guilds/200/members/103")
            .then()
            .statusCode(200);

        // Remove user 103 from guild 200 (restore initial state)
        given()
            .when().delete("/api/guilds/200/members/103")
            .then()
            .statusCode(200);
    }
}