package fr.univtln.yhaouas846.projet.exception;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Structure standardisée pour les réponses d'erreur de l'API.
 *
 * <p>Cette classe représente le format uniforme de toutes les erreurs retournées
 * par l'API REST. Elle suit les bonnes pratiques RFC 7807 (Problem Details for HTTP APIs).</p>
 *
 * <h2>Champs</h2>
 * <ul>
 *   <li>{@code timestamp} : horodatage de l'erreur</li>
 *   <li>{@code status} : code HTTP (400, 404, 500, etc.)</li>
 *   <li>{@code error} : libellé du statut HTTP ("Bad Request", "Not Found", etc.)</li>
 *   <li>{@code message} : message d'erreur détaillé pour le client</li>
 *   <li>{@code path} : chemin de la requête ayant causé l'erreur</li>
 *   <li>{@code details} : détails additionnels (optionnel, ex: erreurs de validation)</li>
 * </ul>
 *
 * <h2>Exemple de réponse JSON</h2>
 * <pre>{@code
 * {
 *   "timestamp": "2026-02-03T14:30:45",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Utilisateur avec id=123 introuvable",
 *   "path": "/api/users/123"
 * }
 * }</pre>
 *
 * @see GlobalExceptionHandler
 * @since 1.0
 */
public class ErrorResponse {
    
    /**
     * Horodatage de l'erreur.
     */
    @NotNull
    public LocalDateTime timestamp;
    
    /**
     * Code de statut HTTP.
     */
    public int status;
    
    /**
     * Libellé du statut HTTP.
     */
    public String error;
    
    /**
     * Message d'erreur détaillé.
     */
    public String message;
    
    /**
     * Chemin de la requête.
     */
    public String path;
    
    /**
     * Détails additionnels (erreurs de validation, stack trace en dev, etc.).
     */
    public Object details;
    
    /**
     * Constructeur par défaut.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur complet.
     *
     * @param status code HTTP
     * @param error libellé du statut
     * @param message message d'erreur
     * @param path chemin de la requête
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    /**
     * Constructeur avec détails additionnels.
     *
     * @param status code HTTP
     * @param error libellé du statut
     * @param message message d'erreur
     * @param path chemin de la requête
     * @param details détails additionnels
     */
    public ErrorResponse(int status, String error, String message, String path, Object details) {
        this(status, error, message, path);
        this.details = details;
    }
}
