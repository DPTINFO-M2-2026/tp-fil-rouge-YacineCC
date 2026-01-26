package fr.univtln.yhaouas846.projet.repository;

import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository Panache pour l'entité {@link fr.univtln.yhaouas846.projet.entity.Role}.
 */
@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {
    /**
     * Retourne le rôle "Member" d'une guilde (rôle par défaut dans la logique du bot).
     *
     * @param guild guilde concernée
     * @return rôle ou {@code null} si absent
     */
    public Role findMemberRole(Guild guild) {
        return find("guild = ?1 AND name = ?2", guild, "Member").firstResult();
    }
}
