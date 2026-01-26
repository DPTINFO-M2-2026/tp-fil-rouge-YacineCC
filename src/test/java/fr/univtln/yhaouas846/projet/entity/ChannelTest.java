package fr.univtln.yhaouas846.projet.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTest {

    private Validator validator;

    private Channel channel;
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
        
        channel = new Channel();
        channel.name = "test-channel";
        channel.description = "A test channel";
        channel.type = Channel.ChannelType.TEXT;
        channel.guild = guild;
        channel.position = 0;
    }

    @Test
    void testValidChannel() {
        Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
        assertTrue(violations.isEmpty(), "Valid channel should not have violations");
    }

    @Test
    void testInvalidChannelName() {
        channel.name = ""; // Trop court
        Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
        assertFalse(violations.isEmpty());
        
        channel.name = "a".repeat(105); // Trop long
        violations = validator.validate(channel);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullType() {
        channel.type = null;
        Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullGuild() {
        channel.guild = null;
        Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDefaultValues() {
        Channel newChannel = new Channel();
        assertEquals(0, newChannel.position, "Position should default to 0");
        assertFalse(newChannel.isNsfw, "isNsfw should default to false");
    }

    @Test
    void testChannelTypes() {
        for (Channel.ChannelType type : Channel.ChannelType.values()) {
            channel.type = type;
            Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
            assertTrue(violations.isEmpty(), "Channel type " + type + " should be valid");
        }
    }

    @Test
    void testPrePersist() {
        assertNull(channel.createdAt);
        channel.prePersist();
        assertNotNull(channel.createdAt);
        assertTrue(channel.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testInvalidPosition() {
        channel.position = -1; // Position négative
        Set<ConstraintViolation<Channel>> violations = validator.validate(channel);
        assertFalse(violations.isEmpty());
    }
}