package fr.univtln.yhaouas846.projet.dto;

import java.time.LocalDateTime;

/**
 * DTO léger représentant un résumé de guilde pour les listes.
 *
 * <p>Contient uniquement les informations essentielles sans charger
 * toutes les relations.</p>
 *
 * @see GuildDTO
 */
public class GuildSummaryDTO {
    
    /**
     * Identifiant unique.
     */
    public Long id;
    
    /**
     * Nom de la guilde.
     */
    public String name;
    
    /**
     * URL de l'icône.
     */
    public String iconUrl;
    
    /**
     * Nom du propriétaire.
     */
    public String ownerName;
    
    /**
     * Nombre de membres.
     */
    public Integer memberCount;
    
    /**
     * Date de création.
     */
    public LocalDateTime createdAt;
    
    /**
     * Constructeur par défaut.
     */
    public GuildSummaryDTO() {
    }
    
    /**
     * Constructeur avec tous les champs.
     *
     * @param id identifiant unique
     * @param name nom de la guilde
     * @param iconUrl URL de l'icône
     * @param ownerName nom du propriétaire
     * @param memberCount nombre de membres
     * @param createdAt date de création
     */
    public GuildSummaryDTO(Long id, String name, String iconUrl, 
                           String ownerName, Integer memberCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.ownerName = ownerName;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }
}
