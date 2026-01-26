package fr.univtln.yhaouas846.projet.repository;

import fr.univtln.yhaouas846.projet.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository Panache pour l'entité {@link fr.univtln.yhaouas846.projet.entity.User}.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    /**
     * Recherche un utilisateur par son {@code username}.
     *
     * @param username nom d'utilisateur
     * @return premier résultat ou {@code null}
     */
    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
