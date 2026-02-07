package fr.univtln.yhaouas846.projet.annotation;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation custom de logging automatique.
 *
 * <p>Placée sur une méthode ou une classe, elle déclenche un logging
 * automatique en entrée et en sortie de chaque appel :</p>
 * <ul>
 *   <li>Entrée : nom de la méthode + arguments</li>
 *   <li>Sortie : nom de la méthode + durée d'exécution</li>
 *   <li>Erreur : exception levée + durée</li>
 * </ul>
 *
 * <h2>Utilisation</h2>
 * <pre>{@code
 * @Logged
 * public UserDTO createUser(CreateUserDTO dto) { ... }
 * }</pre>
 *
 * <p>Au runtime, l'interception est assurée par {@link LoggedInterceptor}
 * via le mécanisme d'intercepteurs CDI de Jakarta.</p>
 *
 * <p>À la compilation, le processeur {@link LoggedProcessor} vérifie que
 * l'annotation est bien posée sur des méthodes publiques et génère un
 * rapport des méthodes annotées.</p>
 *
 * @see LoggedInterceptor
 * @see LoggedProcessor
 * @since 1.0
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Logged {

    /**
     * Niveau de log à utiliser. Par défaut {@code INFO}.
     *
     * @return le niveau de log souhaité
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * Niveaux de log supportés par l'annotation.
     */
    enum LogLevel {
        DEBUG, INFO, WARN
    }
}
