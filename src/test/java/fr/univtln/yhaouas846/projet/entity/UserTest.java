package fr.univtln.yhaouas846.projet.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
class UserTest {

    private Validator validator;

    private User user;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
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
}