package fr.univtln.yhaouas846.projet.annotation;

import fr.univtln.yhaouas846.projet.dto.CreateUserDTO;
import fr.univtln.yhaouas846.projet.service.UserService;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration de l'intercepteur {@link LoggedInterceptor}.
 *
 * <p>Vérifie que les méthodes annotées {@code @Logged} dans {@link UserService}
 * sont correctement interceptées sans altérer le comportement métier.
 * L'intercepteur produit des logs structurés (➡/✅/❌) mais ce test
 * valide uniquement la transparence fonctionnelle.</p>
 */
@QuarkusTest
class LoggedInterceptorTest {

    @Inject
    UserService userService;

    @Test
    void loggedMethod_executesNormally() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.username = "InterceptorTestUser";
        dto.discriminator = "7777";
        dto.email = "interceptor@test.com";
        dto.isBot = false;

        // L'intercepteur ne doit PAS modifier le résultat
        var result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("InterceptorTestUser", result.username);
        assertEquals("7777", result.discriminator);

        // Cleanup
        userService.deleteUser(result.id);
    }

    @Test
    void loggedMethod_propagatesException() {
        // deleteUser sur un id inexistant doit lever ResourceNotFoundException
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(99999L)
        );
    }
}
