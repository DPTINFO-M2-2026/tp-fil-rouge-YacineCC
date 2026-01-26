package fr.univtln.yhaouas846.projet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Test simple pour vérifier que l'application démarre correctement
 */
@QuarkusTest
class GreetingResourceTest {
    
    @Test
    void testHealthEndpoint() {
        // Test que l'endpoint de santé fonctionne
        given()
          .when().get("/api/bot/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
}