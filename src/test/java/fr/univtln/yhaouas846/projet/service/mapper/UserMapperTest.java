package fr.univtln.yhaouas846.projet.service.mapper;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.Role;
import fr.univtln.yhaouas846.projet.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du {@link UserMapper}.
 *
 * <p>Tests purs (pas de Quarkus) : on instancie directement le mapper
 * pour vérifier chaque transformation Entity ↔ DTO.</p>
 */
class UserMapperTest {

    private UserMapper mapper;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();

        sampleUser = new User();
        sampleUser.id = 1L;
        sampleUser.username = "alice";
        sampleUser.discriminator = "1234";
        sampleUser.email = "alice@example.com";
        sampleUser.avatarUrl = "https://cdn.example.com/alice.png";
        sampleUser.discordId = "123456789";
        sampleUser.isBot = false;
        sampleUser.createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        // Relations
        Guild guild = new Guild();
        guild.id = 10L;
        sampleUser.guilds = new HashSet<>(Set.of(guild));

        Role role = new Role();
        role.id = 20L;
        sampleUser.roles = new HashSet<>(Set.of(role));
    }

    // ── toDTO ────────────────────────────────────────────────────────────

    @Test
    void toDTO_mapsAllFields() {
        UserDTO dto = mapper.toDTO(sampleUser);

        assertNotNull(dto);
        assertEquals(1L, dto.id);
        assertEquals("alice", dto.username);
        assertEquals("1234", dto.discriminator);
        assertEquals("alice@example.com", dto.email);
        assertEquals("https://cdn.example.com/alice.png", dto.avatarUrl);
        assertEquals("123456789", dto.discordId);
        assertFalse(dto.isBot);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), dto.createdAt);
    }

    @Test
    void toDTO_mapsGuildAndRoleIds() {
        UserDTO dto = mapper.toDTO(sampleUser);

        assertEquals(Set.of(10L), dto.guildIds);
        assertEquals(Set.of(20L), dto.roleIds);
    }

    @Test
    void toDTO_nullReturnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_nullRelationsAreHandled() {
        sampleUser.guilds = null;
        sampleUser.roles = null;

        UserDTO dto = mapper.toDTO(sampleUser);

        assertNotNull(dto);
        assertNull(dto.guildIds);
        assertNull(dto.roleIds);
    }

    // ── toSummaryDTO ─────────────────────────────────────────────────────

    @Test
    void toSummaryDTO_mapsEssentialFields() {
        UserSummaryDTO dto = mapper.toSummaryDTO(sampleUser);

        assertNotNull(dto);
        assertEquals(1L, dto.id);
        assertEquals("alice", dto.username);
        assertEquals("1234", dto.discriminator);
        assertEquals("https://cdn.example.com/alice.png", dto.avatarUrl);
        assertFalse(dto.isBot);
    }

    @Test
    void toSummaryDTO_nullReturnsNull() {
        assertNull(mapper.toSummaryDTO(null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────

    @Test
    void toEntity_createsUserFromCreateDTO() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.username = "bob";
        dto.discriminator = "5678";
        dto.email = "bob@example.com";
        dto.avatarUrl = "https://cdn.example.com/bob.png";
        dto.discordId = "987654321";
        dto.isBot = true;

        User user = mapper.toEntity(dto);

        assertNotNull(user);
        assertEquals("bob", user.username);
        assertEquals("5678", user.discriminator);
        assertEquals("bob@example.com", user.email);
        assertEquals("987654321", user.discordId);
        assertTrue(user.isBot);
    }

    @Test
    void toEntity_nullReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ── updateEntityFromUpdateDTO ────────────────────────────────────────

    @Test
    void updateEntityFromUpdateDTO_appliesOnlyNonNullFields() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.username = "alice_updated";
        // discriminator, email, avatarUrl restent null → ne doivent pas être écrasés

        mapper.updateEntityFromUpdateDTO(dto, sampleUser);

        assertEquals("alice_updated", sampleUser.username);
        assertEquals("1234", sampleUser.discriminator);         // inchangé
        assertEquals("alice@example.com", sampleUser.email);    // inchangé
        assertEquals("https://cdn.example.com/alice.png", sampleUser.avatarUrl); // inchangé
    }

    @Test
    void updateEntityFromUpdateDTO_nullDTODoesNothing() {
        String originalUsername = sampleUser.username;
        mapper.updateEntityFromUpdateDTO(null, sampleUser);
        assertEquals(originalUsername, sampleUser.username);
    }

    @Test
    void updateEntityFromUpdateDTO_nullUserDoesNothing() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.username = "crash";
        // ne doit pas lever d'exception
        assertDoesNotThrow(() -> mapper.updateEntityFromUpdateDTO(dto, null));
    }

    // ── updateEntityFromCreateDTO ────────────────────────────────────────

    @Test
    void updateEntityFromCreateDTO_overwritesAllFields() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.username = "charlie";
        dto.discriminator = "0000";
        dto.email = "charlie@test.com";
        dto.avatarUrl = null;
        dto.discordId = "111";
        dto.isBot = true;

        mapper.updateEntityFromCreateDTO(dto, sampleUser);

        assertEquals("charlie", sampleUser.username);
        assertEquals("0000", sampleUser.discriminator);
        assertEquals("charlie@test.com", sampleUser.email);
        assertNull(sampleUser.avatarUrl);
        assertEquals("111", sampleUser.discordId);
        assertTrue(sampleUser.isBot);
    }
}
