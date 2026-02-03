package fr.univtln.yhaouas846.projet.service.exception;

/**
 * Exception métier générique levée lorsqu'une règle de gestion est violée.
 *
 * <p>Cette exception représente une erreur métier (business logic error)
 * qui doit être gérée de manière appropriée par la couche présentation
 * (généralement retournée comme erreur HTTP 400 ou 422).</p>
 *
 * <h2>Exemples d'utilisation</h2>
 * <ul>
 *   <li>Tentative de création d'un utilisateur avec un email déjà existant</li>
 *   <li>Violation de contraintes métier (limite de membres dépassée, etc.)</li>
 *   <li>Opération non autorisée selon les règles métier</li>
 * </ul>
 *
 * @see ResourceNotFoundException
 * @since 1.0
 */
public class BusinessException extends RuntimeException {
    
    /**
     * Construit une exception métier avec un message.
     *
     * @param message description de l'erreur métier
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * Construit une exception métier avec un message et une cause.
     *
     * @param message description de l'erreur métier
     * @param cause exception sous-jacente
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
