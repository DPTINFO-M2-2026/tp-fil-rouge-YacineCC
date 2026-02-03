package fr.univtln.yhaouas846.projet.exception;

import fr.univtln.yhaouas846.projet.service.exception.BusinessException;
import fr.univtln.yhaouas846.projet.service.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour l'API REST.
 *
 * <p>Cette classe intercepte toutes les exceptions non gérées dans les ressources REST
 * et les transforme en réponses HTTP standardisées avec un format JSON uniforme.</p>
 *
 * <h2>Responsabilités</h2>
 * <ul>
 *   <li>Transformation des exceptions en réponses HTTP appropriées</li>
 *   <li>Standardisation du format des erreurs (via {@link ErrorResponse})</li>
 *   <li>Logging des erreurs pour le monitoring et le debugging</li>
 *   <li>Masquage des détails techniques en production</li>
 * </ul>
 *
 * <h2>Types d'exceptions gérées</h2>
 * <ul>
 *   <li>{@link ResourceNotFoundException} → HTTP 404</li>
 *   <li>{@link BusinessException} → HTTP 400 ou 422</li>
 *   <li>{@link ConstraintViolationException} → HTTP 400 avec détails de validation</li>
 *   <li>{@link IllegalArgumentException} → HTTP 400</li>
 *   <li>{@link Exception} (générique) → HTTP 500</li>
 * </ul>
 *
 * <h2>Patterns appliqués</h2>
 * <ul>
 *   <li><b>Exception Handler Pattern</b> : centralisation de la gestion d'erreurs</li>
 *   <li><b>Separation of Concerns</b> : les ressources REST ne gèrent pas les erreurs</li>
 *   <li><b>Fail-Safe</b> : toute exception est transformée en réponse HTTP valide</li>
 * </ul>
 *
 * @see ErrorResponse
 * @see ResourceNotFoundException
 * @see BusinessException
 * @since 1.0
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);
    
    @Context
    UriInfo uriInfo;
    
    /**
     * Méthode principale de transformation des exceptions en réponses HTTP.
     *
     * <p>Cette méthode est appelée automatiquement par JAX-RS lorsqu'une exception
     * non gérée est levée dans une ressource REST.</p>
     *
     * @param exception exception à transformer
     * @return réponse HTTP avec un {@link ErrorResponse} en JSON
     */
    @Override
    public Response toResponse(Exception exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        
        // ResourceNotFoundException -> 404
        if (exception instanceof ResourceNotFoundException) {
            return handleResourceNotFoundException((ResourceNotFoundException) exception, path);
        }
        
        // BusinessException -> 422 Unprocessable Entity
        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception, path);
        }
        
        // ConstraintViolationException (validation Jakarta Bean Validation) -> 400
        if (exception instanceof ConstraintViolationException) {
            return handleConstraintViolationException((ConstraintViolationException) exception, path);
        }
        
        // IllegalArgumentException -> 400
        if (exception instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception, path);
        }
        
        // Exception générique -> 500
        return handleGenericException(exception, path);
    }
    
    /**
     * Gère les {@link ResourceNotFoundException} (HTTP 404).
     *
     * @param ex exception
     * @param path chemin de la requête
     * @return réponse HTTP 404
     */
    private Response handleResourceNotFoundException(ResourceNotFoundException ex, String path) {
        LOG.warnf("Ressource non trouvée : %s (path=%s)", ex.getMessage(), path);
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found",
            ex.getMessage(),
            path
        );
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Gère les {@link BusinessException} (HTTP 422 Unprocessable Entity).
     *
     * @param ex exception métier
     * @param path chemin de la requête
     * @return réponse HTTP 422
     */
    private Response handleBusinessException(BusinessException ex, String path) {
        LOG.warnf("Erreur métier : %s (path=%s)", ex.getMessage(), path);
        
        ErrorResponse errorResponse = new ErrorResponse(
            422, // Unprocessable Entity
            "Business Rule Violation",
            ex.getMessage(),
            path
        );
        
        return Response.status(422)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Gère les {@link ConstraintViolationException} (HTTP 400).
     *
     * <p>Extrait les détails de chaque violation de contrainte et les inclut
     * dans la réponse pour faciliter le debugging côté client.</p>
     *
     * @param ex exception de validation
     * @param path chemin de la requête
     * @return réponse HTTP 400 avec détails de validation
     */
    private Response handleConstraintViolationException(ConstraintViolationException ex, String path) {
        LOG.warnf("Erreur de validation : %d violation(s) (path=%s)", 
                  ex.getConstraintViolations().size(), path);
        
        // Extraction des détails de validation
        Map<String, String> violations = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (v1, v2) -> v1 + "; " + v2 // Fusion si plusieurs erreurs sur le même champ
            ));
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Validation Error",
            "Les données fournies ne sont pas valides",
            path,
            violations
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Gère les {@link IllegalArgumentException} (HTTP 400).
     *
     * @param ex exception
     * @param path chemin de la requête
     * @return réponse HTTP 400
     */
    private Response handleIllegalArgumentException(IllegalArgumentException ex, String path) {
        LOG.warnf("Argument invalide : %s (path=%s)", ex.getMessage(), path);
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            ex.getMessage(),
            path
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Gère toutes les autres exceptions (HTTP 500).
     *
     * <p>Log la stack trace complète pour investigation, mais ne retourne
     * qu'un message générique au client pour des raisons de sécurité.</p>
     *
     * @param ex exception non prévue
     * @param path chemin de la requête
     * @return réponse HTTP 500
     */
    private Response handleGenericException(Exception ex, String path) {
        LOG.errorf(ex, "Erreur interne du serveur (path=%s)", path);
        
        // En production, ne pas exposer les détails techniques
        String message = "Une erreur interne s'est produite. Veuillez réessayer plus tard.";
        
        // TODO: En mode développement, on pourrait inclure la stack trace
        // String devMessage = ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace());
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            message,
            path
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorResponse)
            .build();
    }
}
