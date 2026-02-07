package fr.univtln.yhaouas846.projet.annotation;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.util.Arrays;

/**
 * Intercepteur CDI associé à l'annotation {@link Logged}.
 *
 * <p>Intercepte automatiquement les appels aux méthodes annotées {@code @Logged}
 * et produit des logs structurés :</p>
 * <ul>
 *   <li><b>Avant</b> l'appel : classe, méthode, arguments</li>
 *   <li><b>Après</b> l'appel : durée d'exécution en ms</li>
 *   <li><b>En cas d'erreur</b> : type d'exception + durée</li>
 * </ul>
 *
 * <h2>Exemple de sortie</h2>
 * <pre>
 * ➡  UserService.createUser(CreateUserDTO{username=alice, ...})
 * ✅ UserService.createUser → 12ms
 * </pre>
 *
 * @see Logged
 * @since 1.0
 */
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggedInterceptor {

    /**
     * Point de coupe principal. Entoure chaque appel de méthode annotée
     * {@link Logged} avec un log d'entrée et un log de sortie.
     *
     * @param ctx contexte d'invocation CDI
     * @return le résultat de la méthode interceptée
     * @throws Exception toute exception propagée par la méthode cible
     */
    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        String className = ctx.getTarget().getClass().getSimpleName();
        // CDI proxies append _Subclass or _ClientProxy, on nettoie
        if (className.contains("_")) {
            className = className.substring(0, className.indexOf('_'));
        }
        String methodName = ctx.getMethod().getName();
        String args = Arrays.toString(ctx.getParameters());

        Logger log = Logger.getLogger(ctx.getTarget().getClass());

        log.infof("➡  %s.%s(%s)", className, methodName, args);

        long start = System.currentTimeMillis();
        try {
            Object result = ctx.proceed();
            long duration = System.currentTimeMillis() - start;
            log.infof("✅ %s.%s → %dms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.errorf("❌ %s.%s a échoué après %dms : %s", className, methodName, duration, e.getMessage());
            throw e;
        }
    }
}
