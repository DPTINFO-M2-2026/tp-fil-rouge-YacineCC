package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.repository.UserRepository;
import fr.univtln.yhaouas846.projet.service.mapper.UserMapper;
import fr.univtln.yhaouas846.projet.service.exception.BusinessException;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des utilisateurs.
 *
 * <p>Ce service encapsule toute la logique métier liée aux utilisateurs :
 * création, mise à jour, suppression, recherche, validation métier, etc.</p>
 *
 * <h2>Responsabilités</h2>
 * <ul>
 *   <li>CRUD complet sur les utilisateurs</li>
 *   <li>Validation métier (unicité email, discordId)</li>
 *   <li>Transformation entité ↔ DTO via {@link UserMapper}</li>
 *   <li>Gestion des transactions avec {@code @Transactional}</li>
 *   <li>Logging des opérations importantes</li>
 *   <li>Gestion des erreurs métier avec exceptions typées</li>
 * </ul>
 *
 * <h2>Patterns appliqués</h2>
 * <ul>
 *   <li><b>Service Layer Pattern</b> : centralisation de la logique métier</li>
 *   <li><b>Dependency Injection</b> : injection de repository et mapper via CDI</li>
 *   <li><b>Exception Handling</b> : exceptions métier typées</li>
 * </ul>
 *
 * @see User
 * @see UserDTO
 * @see UserMapper
 * @see UserRepository
 * @since 1.0
 */
@ApplicationScoped
public class UserService {
    
    private static final Logger LOG = Logger.getLogger(UserService.class);
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserMapper userMapper;
    
    /**
     * Récupère tous les utilisateurs sous forme de DTOs résumés.
     *
     * @return liste de tous les utilisateurs (peut être vide)
     */
    public List<UserSummaryDTO> getAllUsers() {
        LOG.debug("Récupération de tous les utilisateurs");
        return User.<User>listAll().stream()
            .map(userMapper::toSummaryDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return DTO complet de l'utilisateur
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    public UserDTO getUserById(Long id) {
        LOG.debugf("Récupération de l'utilisateur avec id=%d", id);
        User user = User.findById(id);
        
        if (user == null) {
            LOG.warnf("Utilisateur avec id=%d introuvable", id);
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        
        return userMapper.toDTO(user);
    }
    
    /**
     * Recherche un utilisateur par nom d'utilisateur.
     *
     * @param username nom d'utilisateur à rechercher
     * @return DTO de l'utilisateur trouvé
     * @throws ResourceNotFoundException si aucun utilisateur ne correspond
     */
    public UserDTO getUserByUsername(String username) {
        LOG.debugf("Recherche de l'utilisateur avec username=%s", username);
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            LOG.warnf("Aucun utilisateur trouvé avec username=%s", username);
            throw new ResourceNotFoundException("Utilisateur avec username: " + username);
        }
        
        return userMapper.toDTO(user);
    }
    
    /**
     * Récupère la liste des comptes bots.
     *
     * @return liste des utilisateurs marqués comme bots
     */
    public List<UserSummaryDTO> getBotUsers() {
        LOG.debug("Récupération des comptes bots");
        return User.<User>list("isBot", true).stream()
            .map(userMapper::toSummaryDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Crée un nouvel utilisateur.
     *
     * <p><b>Validations métier :</b></p>
     * <ul>
     *   <li>L'email doit être unique (s'il est fourni)</li>
     *   <li>Le discordId doit être unique (s'il est fourni)</li>
     * </ul>
     *
     * @param createDTO données de création
     * @return DTO de l'utilisateur créé
     * @throws BusinessException si les contraintes d'unicité ne sont pas respectées
     */
    @Transactional
    public UserDTO createUser(CreateUserDTO createDTO) {
        LOG.infof("Création d'un nouvel utilisateur : %s#%s", createDTO.username, createDTO.discriminator);
        
        // Validation métier : unicité de l'email
        if (createDTO.email != null) {
            User existingByEmail = User.find("email", createDTO.email).firstResult();
            if (existingByEmail != null) {
                LOG.warnf("Tentative de création avec email déjà existant: %s", createDTO.email);
                throw new BusinessException("Un utilisateur avec cet email existe déjà");
            }
        }
        
        // Validation métier : unicité du discordId
        if (createDTO.discordId != null) {
            User existingByDiscordId = User.find("discordId", createDTO.discordId).firstResult();
            if (existingByDiscordId != null) {
                LOG.warnf("Tentative de création avec discordId déjà existant: %s", createDTO.discordId);
                throw new BusinessException("Un utilisateur avec ce discordId existe déjà");
            }
        }
        
        // Création et persistance
        User user = userMapper.toEntity(createDTO);
        user.persist();
        
        LOG.infof("Utilisateur créé avec succès : id=%d", user.id);
        return userMapper.toDTO(user);
    }
    
    /**
     * Met à jour un utilisateur existant.
     *
     * <p>Effectue une mise à jour partielle : seuls les champs non-null du DTO
     * sont appliqués à l'entité.</p>
     *
     * @param id identifiant de l'utilisateur à mettre à jour
     * @param updateDTO données de mise à jour
     * @return DTO de l'utilisateur mis à jour
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     * @throws BusinessException si les contraintes d'unicité ne sont pas respectées
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO updateDTO) {
        LOG.infof("Mise à jour de l'utilisateur id=%d", id);
        
        User user = User.findById(id);
        if (user == null) {
            LOG.warnf("Tentative de mise à jour d'un utilisateur inexistant: id=%d", id);
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        
        // Validation métier : unicité de l'email (si modifié)
        if (updateDTO.email != null && !updateDTO.email.equals(user.email)) {
            User existingByEmail = User.find("email", updateDTO.email).firstResult();
            if (existingByEmail != null && !existingByEmail.id.equals(id)) {
                LOG.warnf("Tentative de mise à jour avec email déjà existant: %s", updateDTO.email);
                throw new BusinessException("Un autre utilisateur avec cet email existe déjà");
            }
        }
        
        // Application des modifications
        userMapper.updateEntityFromUpdateDTO(updateDTO, user);
        user.persist();
        
        LOG.infof("Utilisateur mis à jour avec succès : id=%d", user.id);
        return userMapper.toDTO(user);
    }
    
    /**
     * Supprime un utilisateur.
     *
     * <p><b>Note :</b> Cette opération est définitive. Les messages et autres données
     * liées seront gérées selon les cascades JPA définies.</p>
     *
     * @param id identifiant de l'utilisateur à supprimer
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public void deleteUser(Long id) {
        LOG.infof("Suppression de l'utilisateur id=%d", id);
        
        User user = User.findById(id);
        if (user == null) {
            LOG.warnf("Tentative de suppression d'un utilisateur inexistant: id=%d", id);
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        
        user.delete();
        LOG.infof("Utilisateur supprimé avec succès : id=%d", id);
    }
}
