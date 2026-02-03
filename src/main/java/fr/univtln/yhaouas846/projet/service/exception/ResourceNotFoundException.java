package fr.univtln.yhaouas846.projet.service.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée.
 *
 * <p>Cette exception représente une erreur de type "Not Found" (HTTP 404).
 * Elle est levée lors de recherches par identifiant, par nom d'utilisateur,
 * ou toute autre recherche qui n'aboutit pas.</p>
 *
 * <h2>Exemples d'utilisation</h2>
 * <ul>
 *   <li>Recherche d'un utilisateur par ID inexistant</li>
 *   <li>Recherche d'une guilde qui n'existe pas</li>
 *   <li>Tentative d'accès à un canal supprimé</li>
 * </ul>
 *
 * @see BusinessException
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final Object resourceId;
    
    /**
     * Construit une exception de ressource non trouvée.
     *
     * @param message message descriptif
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId = null;
    }
    
    /**
     * Construit une exception de ressource non trouvée avec type et identifiant.
     *
     * @param resourceType type de ressource (ex: "Utilisateur", "Guilde")
     * @param resourceId identifiant de la ressource
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s avec id=%s introuvable", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Retourne le type de ressource non trouvée.
     *
     * @return type de ressource
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Retourne l'identifiant de la ressource non trouvée.
     *
     * @return identifiant de la ressource
     */
    public Object getResourceId() {
        return resourceId;
    }
}
