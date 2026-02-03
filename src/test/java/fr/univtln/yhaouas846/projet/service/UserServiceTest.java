package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.*;
import fr.univtln.yhaouas846.projet.service.exception.BusinessException;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour {@link UserService}.
 *
 * <p>Ces tests vérifient le bon fonctionnement de la logique métier
 * du service utilisateur, y compris les validations, les transformations DTO,
 * et la gestion des erreurs.</p>
 *
 * <h2>Couverture des tests</h2>
 * <ul>
 *   <li>Création d'utilisateur (cas nominal et erreurs)</li>
 *   <li>Récupération d'utilisateur (par ID, par username)</li>
 *   <li>Mise à jour d'utilisateur (partielle)</li>
 *   <li>Suppression d'utilisateur</li>
 *   <li>Validation métier (unicité email, discordId)</li>
 *   <li>Gestion des exceptions (ResourceNotFoundException, BusinessException)</li>
 * </ul>
 *
 * @see UserService
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    
    @Inject
    UserService userService;
    
    private Long createdUserId;
    
    /**
     * Nettoie la base de données avant chaque test.
     * Utilise du SQL natif pour supprimer dans l'ordre correct et gérer les tables de jointure.
     */
    @BeforeEach
    @Transactional
    void setUp() {
        // Supprimer toutes les tables dans l'ordre inverse des dépendances
        jakarta.persistence.EntityManager em = User.getEntityManager();
        
        // Tables de jointure
        em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        em.createNativeQuery("DELETE FROM guild_members").executeUpdate();
        
        // Tables avec clés étrangères
        em.createNativeQuery("DELETE FROM message").executeUpdate();
        em.createNativeQuery("DELETE FROM channel").executeUpdate();
        em.createNativeQuery("DELETE FROM role").executeUpdate();
        em.createNativeQuery("DELETE FROM guild").executeUpdate();
        em.createNativeQuery("DELETE FROM discord_user").executeUpdate();
    }
    
    /**
     * Test de création d'un utilisateur (cas nominal).
     */
    @Test
    @Order(1)
    void testCreateUser_Success() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.username = "testuser";
        createDTO.discriminator = "1234";
        createDTO.email = "test@example.com";
        createDTO.isBot = false;
        
        // When
        UserDTO result = userService.createUser(createDTO);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.id);
        assertEquals("testuser", result.username);
        assertEquals("1234", result.discriminator);
        assertEquals("test@example.com", result.email);
        assertFalse(result.isBot);
        assertNotNull(result.createdAt);
        
        createdUserId = result.id;
    }
    
    /**
     * Test de création avec email déjà existant (doit échouer).
     */
    @Test
    @Order(2)
    void testCreateUser_DuplicateEmail_ShouldFail() {
        // Given
        CreateUserDTO firstUser = new CreateUserDTO();
        firstUser.username = "user1";
        firstUser.discriminator = "0001";
        firstUser.email = "duplicate@example.com";
        userService.createUser(firstUser);
        
        CreateUserDTO secondUser = new CreateUserDTO();
        secondUser.username = "user2";
        secondUser.discriminator = "0002";
        secondUser.email = "duplicate@example.com"; // Email déjà utilisé
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(secondUser);
        });
        
        assertTrue(exception.getMessage().contains("email"));
    }
    
    /**
     * Test de création avec discordId déjà existant (doit échouer).
     */
    @Test
    @Order(3)
    void testCreateUser_DuplicateDiscordId_ShouldFail() {
        // Given
        CreateUserDTO firstUser = new CreateUserDTO();
        firstUser.username = "user1";
        firstUser.discriminator = "0001";
        firstUser.discordId = "123456789";
        userService.createUser(firstUser);
        
        CreateUserDTO secondUser = new CreateUserDTO();
        secondUser.username = "user2";
        secondUser.discriminator = "0002";
        secondUser.discordId = "123456789"; // DiscordId déjà utilisé
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(secondUser);
        });
        
        assertTrue(exception.getMessage().contains("discordId"));
    }
    
    /**
     * Test de récupération d'un utilisateur par ID.
     */
    @Test
    @Order(4)
    void testGetUserById_Success() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.username = "findme";
        createDTO.discriminator = "5678";
        UserDTO created = userService.createUser(createDTO);
        
        // When
        UserDTO found = userService.getUserById(created.id);
        
        // Then
        assertNotNull(found);
        assertEquals(created.id, found.id);
        assertEquals("findme", found.username);
    }
    
    /**
     * Test de récupération d'un utilisateur inexistant (doit échouer).
     */
    @Test
    @Order(5)
    void testGetUserById_NotFound_ShouldFail() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99999L);
        });
    }
    
    /**
     * Test de récupération par nom d'utilisateur.
     */
    @Test
    @Order(6)
    void testGetUserByUsername_Success() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.username = "uniquename";
        createDTO.discriminator = "9999";
        userService.createUser(createDTO);
        
        // When
        UserDTO found = userService.getUserByUsername("uniquename");
        
        // Then
        assertNotNull(found);
        assertEquals("uniquename", found.username);
    }
    
    /**
     * Test de mise à jour d'un utilisateur (partielle).
     */
    @Test
    @Order(7)
    void testUpdateUser_Success() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.username = "oldname";
        createDTO.discriminator = "0000";
        createDTO.email = "old@example.com";
        UserDTO created = userService.createUser(createDTO);
        
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.username = "newname";
        updateDTO.email = "new@example.com";
        // discriminator n'est pas mis à jour
        
        // When
        UserDTO updated = userService.updateUser(created.id, updateDTO);
        
        // Then
        assertEquals("newname", updated.username);
        assertEquals("new@example.com", updated.email);
        assertEquals("0000", updated.discriminator); // Inchangé
    }
    
    /**
     * Test de mise à jour avec email déjà utilisé par un autre utilisateur.
     */
    @Test
    @Order(8)
    void testUpdateUser_DuplicateEmail_ShouldFail() {
        // Given
        CreateUserDTO user1 = new CreateUserDTO();
        user1.username = "user1";
        user1.discriminator = "0001";
        user1.email = "email1@example.com";
        userService.createUser(user1);
        
        CreateUserDTO user2 = new CreateUserDTO();
        user2.username = "user2";
        user2.discriminator = "0002";
        user2.email = "email2@example.com";
        UserDTO created2 = userService.createUser(user2);
        
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.email = "email1@example.com"; // Email déjà utilisé par user1
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.updateUser(created2.id, updateDTO);
        });
    }
    
    /**
     * Test de suppression d'un utilisateur.
     */
    @Test
    @Order(9)
    void testDeleteUser_Success() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.username = "tobedeleted";
        createDTO.discriminator = "9999";
        UserDTO created = userService.createUser(createDTO);
        
        // When
        userService.deleteUser(created.id);
        
        // Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(created.id);
        });
    }
    
    /**
     * Test de suppression d'un utilisateur inexistant.
     */
    @Test
    @Order(10)
    void testDeleteUser_NotFound_ShouldFail() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(99999L);
        });
    }
    
    /**
     * Test de récupération des comptes bots.
     */
    @Test
    @Order(11)
    void testGetBotUsers() {
        // Given
        CreateUserDTO bot = new CreateUserDTO();
        bot.username = "mybot";
        bot.discriminator = "0001";
        bot.isBot = true;
        userService.createUser(bot);
        
        CreateUserDTO human = new CreateUserDTO();
        human.username = "human";
        human.discriminator = "0002";
        human.isBot = false;
        userService.createUser(human);
        
        // When
        var bots = userService.getBotUsers();
        
        // Then
        assertEquals(1, bots.size());
        assertTrue(bots.get(0).isBot);
        assertEquals("mybot", bots.get(0).username);
    }
}
