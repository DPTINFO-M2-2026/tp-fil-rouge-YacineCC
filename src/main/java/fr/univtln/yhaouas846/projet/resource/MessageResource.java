package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Message;
import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST de gestion des messages.
 *
 * <p>Cette API propose un CRUD et plusieurs endpoints de consultation :
 * filtrage par canal, par auteur, et recherche textuelle sur le contenu.</p>
 *
 * <p>Le modèle utilise une suppression logique : le {@code DELETE} ne supprime pas
 * physiquement la ligne mais positionne {@code isDeleted=true}. Les requêtes de
 * consultation filtrent généralement sur {@code isDeleted=false}.</p>
 */
@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @GET
    public List<Message> getAllMessages(@QueryParam("limit") @DefaultValue("50") int limit) {
        return Message.find("ORDER BY createdAt DESC").page(0, limit).list();
    }

    @GET
    @Path("/{id}")
    public Response getMessageById(@PathParam("id") Long id) {
        Message message = Message.findById(id);
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(message).build();
    }

    /**
     * Crée un message.
     *
     * <p>Si {@code discordId} est présent et déjà connu, le message existant est mis à jour
     * (utile pour un mode sync). Sinon, une nouvelle entité est persistée.</p>
     *
     * @param message message à créer/mettre à jour
     * @return {@code 201} si création, {@code 200} si mise à jour, {@code 400} en cas d'erreur
     */
    @POST
    @Transactional
    public Response createMessage(@Valid Message message) {
        try {
            // Check if message already exists by discordId
            if (message.discordId != null) {
                Message existingMessage = Message.find("discordId", message.discordId).firstResult();
                if (existingMessage != null) {
                    // Update existing message
                    existingMessage.content = message.content;
                    existingMessage.isEdited = message.isEdited;
                    existingMessage.updatedAt = message.updatedAt;
                    existingMessage.persist();
                    return Response.ok(existingMessage).build();
                }
            }

            message.persist();
            return Response.status(Response.Status.CREATED).entity(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating message: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateMessage(@PathParam("id") Long id, @Valid Message updatedMessage) {
        Message message = Message.findById(id);
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        message.content = updatedMessage.content;
        message.embedTitle = updatedMessage.embedTitle;
        message.embedDescription = updatedMessage.embedDescription;
        message.embedColor = updatedMessage.embedColor;
        message.attachmentUrl = updatedMessage.attachmentUrl;
        
        try {
            message.persist();
            return Response.ok(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating message: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Supprime logiquement un message.
     *
     * <p>Le message est marqué supprimé via {@code isDeleted=true} et reste présent en base.</p>
     *
     * @param id identifiant du message
     * @return {@code 204} si succès, {@code 404} si introuvable
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteMessage(@PathParam("id") Long id) {
        Message message = Message.findById(id);
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        message.isDeleted = true;
        message.persist();
        return Response.noContent().build();
    }

    @GET
    @Path("/channel/{channelId}")
    public Response getMessagesByChannel(@PathParam("channelId") Long channelId, 
                                       @QueryParam("limit") @DefaultValue("50") int limit,
                                       @QueryParam("offset") @DefaultValue("0") int offset) {
        Channel channel = Channel.findById(channelId);
        if (channel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<Message> messages = Message.find("channel = ?1 AND isDeleted = false ORDER BY createdAt DESC", channel)
                                       .page(offset / limit, limit)
                                       .list();
        return Response.ok(messages).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getMessagesByUser(@PathParam("userId") Long userId,
                                    @QueryParam("limit") @DefaultValue("50") int limit) {
        User user = User.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<Message> messages = Message.find("author = ?1 AND isDeleted = false ORDER BY createdAt DESC", user)
                                       .page(0, limit)
                                       .list();
        return Response.ok(messages).build();
    }

    @GET
    @Path("/search")
    /**
     * Recherche des messages par sous-chaîne sur le champ {@code content}.
     *
     * <p>Si le terme de recherche est vide, la réponse est une liste vide.</p>
     *
     * @param content terme à rechercher
     * @param limit limite de résultats (par défaut 20)
     * @return liste de messages non supprimés correspondant au filtre
     */
    public List<Message> searchMessages(@QueryParam("content") String content,
                                      @QueryParam("limit") @DefaultValue("20") int limit) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }
        
        return Message.find("content LIKE ?1 AND isDeleted = false ORDER BY createdAt DESC", 
                           "%" + content + "%")
                     .page(0, limit)
                     .list();
    }
}