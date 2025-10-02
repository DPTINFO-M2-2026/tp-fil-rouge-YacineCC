package fr.univtln.yhaouas846.projet.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GuildTest {

    @Inject
    Validator validator;

    private Guild guild;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.username = "Owner";
        owner.discriminator = "0001";
        
        guild = new Guild();
        guild.name = "Test Guild";
        guild.description = "A test guild";
        guild.owner = owner;
        guild.memberLimit = 1000;
    }

    @Test
    void testValidGuild() {
        Set<ConstraintViolation<Guild>> violations = validator.validate(guild);
        assertTrue(violations.isEmpty(), "Valid guild should not have violations");
    }

    @Test
    void testInvalidGuildName() {
        guild.name = "a"; // Trop court
        Set<ConstraintViolation<Guild>> violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
        
        guild.name = "a".repeat(105); // Trop long
        violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidMemberLimit() {
        guild.memberLimit = 1; // Trop petit
        Set<ConstraintViolation<Guild>> violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
        
        guild.memberLimit = 900000; // Trop grand
        violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullOwner() {
        guild.owner = null;
        Set<ConstraintViolation<Guild>> violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDefaultMemberLimit() {
        Guild newGuild = new Guild();
        assertEquals(500000, newGuild.memberLimit, "Default member limit should be 500000");
    }

    @Test
    void testPrePersist() {
        assertNull(guild.createdAt);
        guild.prePersist();
        assertNotNull(guild.createdAt);
        assertTrue(guild.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testLongDescription() {
        guild.description = "a".repeat(1025); // Trop long
        Set<ConstraintViolation<Guild>> violations = validator.validate(guild);
        assertFalse(violations.isEmpty());
    }
}