package fr.univtln.yhaouas846.projet.repository;

import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Message;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository Panache pour l'entité {@link fr.univtln.yhaouas846.projet.entity.Message}.
 */
@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {
    /**
     * Retourne les derniers messages non supprimés d'un canal, triés par date décroissante.
     *
     * @param channel canal cible
     * @param limit nombre maximum d'éléments (pagination Panache)
     * @return liste de messages
     */
    public List<Message> findChannelMessages(Channel channel, int limit) {
        return find("channel = ?1 AND isDeleted = false ORDER BY createdAt DESC", channel)
                .page(0, limit)
                .list();
    }
}
