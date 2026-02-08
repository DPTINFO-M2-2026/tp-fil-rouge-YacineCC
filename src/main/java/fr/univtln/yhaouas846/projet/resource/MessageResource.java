package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.service.MessageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST de gestion des messages.
 *
 * <p>Cette ressource délègue la logique métier au {@link MessageService}
 * et utilise des DTOs pour le contrat API.</p>
 *
 * <p>Le modèle utilise une suppression logique : le {@code DELETE} ne supprime pas
 * physiquement la ligne mais positionne {@code isDeleted=true}.</p>
 */
@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    MessageService messageService;

    /**
     * Liste les messages les plus récents.
     */
    @GET
    public List<MessageDTO> getAllMessages(@QueryParam("limit") @DefaultValue("50") int limit) {
        return messageService.getAllMessages(limit);
    }

    /**
     * Récupère un message par son identifiant.
     */
    @GET
    @Path("/{id}")
    public MessageDTO getMessageById(@PathParam("id") Long id) {
        return messageService.getMessageById(id);
    }

    /**
     * Crée un message.
     *
     * <p>Si {@code discordId} est présent et déjà connu, le message existant est mis à jour
     * (utile pour un mode sync).</p>
     *
     * @param dto données de création
     * @return {@code 201} avec le DTO du message créé
     */
    @POST
    public Response createMessage(@Valid CreateMessageDTO dto) {
        MessageDTO created = messageService.createMessage(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Met à jour un message existant.
     */
    @PUT
    @Path("/{id}")
    public MessageDTO updateMessage(@PathParam("id") Long id, @Valid CreateMessageDTO dto) {
        return messageService.updateMessage(id, dto);
    }

    /**
     * Supprime logiquement un message.
     *
     * <p>Le message est marqué supprimé via {@code isDeleted=true} et reste présent en base.</p>
     *
     * @param id identifiant du message
     * @return {@code 204} si succès
     */
    @DELETE
    @Path("/{id}")
    public Response deleteMessage(@PathParam("id") Long id) {
        messageService.deleteMessage(id);
        return Response.noContent().build();
    }

    /**
     * Liste les messages d'un canal (non supprimés).
     */
    @GET
    @Path("/channel/{channelId}")
    public List<MessageDTO> getMessagesByChannel(@PathParam("channelId") Long channelId,
                                                 @QueryParam("limit") @DefaultValue("50") int limit,
                                                 @QueryParam("offset") @DefaultValue("0") int offset) {
        return messageService.getMessagesByChannel(channelId, limit, offset);
    }

    /**
     * Liste les messages d'un utilisateur (non supprimés).
     */
    @GET
    @Path("/user/{userId}")
    public List<MessageDTO> getMessagesByUser(@PathParam("userId") Long userId,
                                              @QueryParam("limit") @DefaultValue("50") int limit) {
        return messageService.getMessagesByUser(userId, limit);
    }

    /**
     * Recherche des messages par sous-chaîne sur le champ {@code content}.
     *
     * @param content terme à rechercher
     * @param limit limite de résultats (par défaut 20)
     * @return liste de messages non supprimés correspondant au filtre
     */
    @GET
    @Path("/search")
    public List<MessageDTO> searchMessages(@QueryParam("content") String content,
                                           @QueryParam("limit") @DefaultValue("20") int limit) {
        return messageService.searchMessages(content, limit);
    }
}