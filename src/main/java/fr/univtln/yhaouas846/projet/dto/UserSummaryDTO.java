package fr.univtln.yhaouas846.projet.dto;

/**
 * DTO léger représentant un résumé d'utilisateur pour les listes.
 *
 * <p>Ce DTO contient uniquement les informations essentielles pour afficher
 * une liste d'utilisateurs sans charger toutes les relations.</p>
 *
 * @see UserDTO
 */
public class UserSummaryDTO {
    
    /**
     * Identifiant unique.
     */
    public Long id;
    
    /**
     * Nom d'utilisateur.
     */
    public String username;
    
    /**
     * Discriminator.
     */
    public String discriminator;
    
    /**
     * URL de l'avatar.
     */
    public String avatarUrl;
    
    /**
     * Indicateur si l'utilisateur est un bot.
     */
    public boolean isBot;
    
    /**
     * Constructeur par défaut.
     */
    public UserSummaryDTO() {
    }
    
    /**
     * Constructeur avec tous les champs.
     *
     * @param id identifiant unique
     * @param username nom d'utilisateur
     * @param discriminator discriminator
     * @param avatarUrl URL de l'avatar
     * @param isBot indicateur bot
     */
    public UserSummaryDTO(Long id, String username, String discriminator, 
                          String avatarUrl, boolean isBot) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatarUrl = avatarUrl;
        this.isBot = isBot;
    }
}
