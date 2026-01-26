package fr.univtln.yhaouas846.projet.repository;

import fr.univtln.yhaouas846.projet.entity.Channel;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository Panache pour l'entité {@link fr.univtln.yhaouas846.projet.entity.Channel}.
 *
 * <p>Actuellement sans méthode custom : les opérations CRUD sont fournies par Panache.</p>
 */
@ApplicationScoped
public class ChannelRepository implements PanacheRepository<Channel> {
}
