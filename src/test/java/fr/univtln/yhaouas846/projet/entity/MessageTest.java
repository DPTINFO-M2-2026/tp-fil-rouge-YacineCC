package fr.univtln.yhaouas846.projet.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private Validator validator;

    private Message message;
    private User author;
    private Channel channel;
    private Guild guild;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        User owner = new User();
        owner.username = "Owner";
        owner.discriminator = "0001";
        
        author = new User();
        author.username = "Author";
        author.discriminator = "1234";
        
        guild = new Guild();
        guild.name = "Test Guild";
        guild.owner = owner;
        
        channel = new Channel();
        channel.name = "test-channel";
        channel.type = Channel.ChannelType.TEXT;
        channel.guild = guild;
        
        message = new Message();
        message.content = "Test message content";
        message.author = author;
        message.channel = channel;
    }

    @Test
    void testValidMessage() {
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Valid message should not have violations");
    }

    @Test
    void testInvalidContent() {
        message.content = ""; // Contenu vide
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertFalse(violations.isEmpty());
        
        message.content = "a".repeat(2005); // Trop long
        violations = validator.validate(message);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullAuthor() {
        message.author = null;
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullChannel() {
        message.channel = null;
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDefaultValues() {
        Message newMessage = new Message();
        assertFalse(newMessage.isEdited, "isEdited should default to false");
        assertFalse(newMessage.isDeleted, "isDeleted should default to false");
    }

    @Test
    void testPrePersist() {
        assertNull(message.createdAt);
        message.prePersist();
        assertNotNull(message.createdAt);
        assertTrue(message.createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testPreUpdate() {
        message.createdAt = LocalDateTime.now().minusHours(1);
        assertNull(message.updatedAt);
        assertFalse(message.isEdited);
        
        message.preUpdate();
        
        assertNotNull(message.updatedAt);
        assertTrue(message.isEdited);
        assertTrue(message.updatedAt.isAfter(message.createdAt));
    }

    @Test
    void testEmbedFields() {
        message.embedTitle = "Test Title";
        message.embedDescription = "Test Description";
        message.embedColor = "#FF0000";
        
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Message with embed fields should be valid");
    }

    @Test
    void testInvalidEmbedTitle() {
        message.embedTitle = "a".repeat(300); // Trop long (max 256)
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidEmbedDescription() {
        message.embedDescription = "a".repeat(4100); // Trop long
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        assertFalse(violations.isEmpty());
    }
}