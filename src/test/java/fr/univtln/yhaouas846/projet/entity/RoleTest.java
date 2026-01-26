package fr.univtln.yhaouas846.projet.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Validator validator;

    private Role role;
    private Guild guild;
    private User owner;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        owner = new User();
        owner.username = "Owner";
        owner.discriminator = "0001";
        
        guild = new Guild();
        guild.name = "Test Guild";
        guild.owner = owner;
        
        role = new Role();
        role.name = "Test Role";
        role.color = "#FF0000";
        role.guild = guild;
        role.position = 1;
    }

    @Test
    void testValidRole() {
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertTrue(violations.isEmpty(), "Valid role should not have violations");
    }

    @Test
    void testInvalidRoleName() {
        role.name = ""; // Nom vide
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        
        role.name = "a".repeat(105); // Trop long
        violations = validator.validate(role);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidColor() {
        role.color = "FF0000"; // Sans #
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        
        role.color = "#GG0000"; // Caractères invalides
        violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        
        role.color = "#FF00"; // Trop court
        violations = validator.validate(role);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidColors() {
        String[] validColors = {"#FF0000", "#00FF00", "#0000FF", "#ffffff", "#000000", "#123abc"};
        
        for (String color : validColors) {
            role.color = color;
            Set<ConstraintViolation<Role>> violations = validator.validate(role);
            assertTrue(violations.isEmpty(), "Color " + color + " should be valid");
        }
    }

    @Test
    void testNullGuild() {
        role.guild = null;
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDefaultValues() {
        Role newRole = new Role();
        assertEquals("#000000", newRole.color, "Color should default to #000000");
        assertEquals(0, newRole.position, "Position should default to 0");
        assertFalse(newRole.canManageChannels);
        assertFalse(newRole.canManageRoles);
        assertFalse(newRole.canManageMessages);
        assertFalse(newRole.canKickMembers);
        assertFalse(newRole.canBanMembers);
        assertTrue(newRole.canSendMessages, "canSendMessages should default to true");
        assertTrue(newRole.canReadMessages, "canReadMessages should default to true");
    }

    @Test
    void testPrePersist() {
        assertNull(role.createdAt);
        role.prePersist();
        assertNotNull(role.createdAt);
        assertTrue(role.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testInvalidPosition() {
        role.position = -1; // Position négative
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        assertFalse(violations.isEmpty());
    }
}