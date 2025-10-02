package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserTest {

    @Inject
    Validator validator;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.username = "TestUser";
        user.discriminator = "1234";
        user.email = "test@example.com";
        user.avatarUrl = "https://example.com/avatar.png";
        user.isBot = false;
    }

    @Test
    void testValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should not have violations");
    }

    @Test
    void testInvalidUsername() {
        user.username = "a"; // Trop court
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        
        user.username = "a".repeat(35); // Trop long
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidDiscriminator() {
        user.discriminator = "123"; // Trop court
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        
        user.discriminator = "abcd"; // Pas des chiffres
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidEmail() {
        user.email = "invalid-email";
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testBlankUsername() {
        user.username = "";
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        
        user.username = null;
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPrePersist() {
        assertNull(user.createdAt);
        user.prePersist();
        assertNotNull(user.createdAt);
        assertTrue(user.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testDefaultValues() {
        User newUser = new User();
        assertFalse(newUser.isBot, "isBot should default to false");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUserRelationsWithMocks() {
        // Mock des collections pour tester les relations
        Set<Message> mockMessages = (Set<Message>) mock(Set.class);
        Set<Guild> mockGuilds = (Set<Guild>) mock(Set.class);
        Set<Role> mockRoles = (Set<Role>) mock(Set.class);
        
        user.messages = mockMessages;
        user.guilds = mockGuilds;
        user.roles = mockRoles;
        
        // Vérifier que les relations sont bien assignées
        assertNotNull(user.messages);
        assertNotNull(user.guilds);
        assertNotNull(user.roles);
        
        // Simuler des interactions avec les collections
        when(mockMessages.size()).thenReturn(5);
        when(mockGuilds.size()).thenReturn(2);
        when(mockRoles.size()).thenReturn(3);
        
        assertEquals(5, user.messages.size());
        assertEquals(2, user.guilds.size());
        assertEquals(3, user.roles.size());
        
        // Vérifier que les méthodes ont été appelées
        verify(mockMessages).size();
        verify(mockGuilds).size();
        verify(mockRoles).size();
    }
}