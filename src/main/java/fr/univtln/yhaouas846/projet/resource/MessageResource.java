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