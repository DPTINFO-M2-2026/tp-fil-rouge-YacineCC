package fr.univtln.yhaouas846.projet.repository;

import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository Panache pour l'entité {@link fr.univtln.yhaouas846.projet.entity.Guild}.
 *
 * <p>Centralise des requêtes de lecture un peu plus riches que les opérations
 * CRUD de base.</p>
 */
@ApplicationScoped
public class GuildRepository implements PanacheRepository<Guild> {
    /**
     * Retourne les guildes pour lesquelles l'utilisateur est propriétaire ou membre.
     *
     * @param user utilisateur recherché
     * @return liste de guildes associées
     */
    public List<Guild> findByOwnerOrMember(User user) {
        return find("owner = ?1 OR ?2 MEMBER OF members", user, user).list();
    }
}
