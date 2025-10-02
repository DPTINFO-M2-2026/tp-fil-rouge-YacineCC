package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.Message;
import fr.univtln.yhaouas846.projet.service.DiscordBotService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/bot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BotResource {

    @Inject
    DiscordBotService discordBotService;

    @POST
    @Path("/guilds")
    public Response createGuildWithDefaults(@QueryParam("name") String guildName, 
                                          @QueryParam("owner") String ownerUsername) {
        try {
            Guild guild = discordBotService.createGuildWithDefaultChannels(guildName, ownerUsername);
            return Response.status(Response.Status.CREATED).entity(guild).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/messages")
    public Response sendMessage(@QueryParam("authorId") Long authorId,
                              @QueryParam("channelId") Long channelId,
                              @QueryParam("content") String content) {
        try {
            Message message = discordBotService.sendMessage(authorId, channelId, content);
            return Response.status(Response.Status.CREATED).entity(message).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/messages/channel/{channelId}")
    public Response getChannelMessages(@PathParam("channelId") Long channelId,
                                     @QueryParam("limit") @DefaultValue("50") int limit) {
        try {
            List<Message> messages = discordBotService.getChannelMessages(channelId, limit);
            return Response.ok(messages).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/guilds/{guildId}/members/{userId}")
    public Response addUserToGuild(@PathParam("guildId") Long guildId,
                                 @PathParam("userId") Long userId) {
        try {
            discordBotService.addUserToGuild(userId, guildId);
            return Response.ok(Map.of("message", "User added to guild successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/users/{userId}/guilds")
    public Response getUserGuilds(@PathParam("userId") Long userId) {
        try {
            List<Guild> guilds = discordBotService.getUserGuilds(userId);
            return Response.ok(guilds).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/messages/{messageId}")
    public Response deleteMessage(@PathParam("messageId") Long messageId,
                                @QueryParam("requesterId") Long requesterId) {
        try {
            discordBotService.deleteMessage(messageId, requesterId);
            return Response.ok(Map.of("message", "Message deleted successfully")).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/users/{userId}/permissions")
    public Response checkUserPermissions(@PathParam("userId") Long userId) {
        try {
            boolean isBotOwner = discordBotService.isUserBotOwner(userId);
            return Response.ok(Map.of(
                "userId", userId,
                "isBotOwner", isBotOwner
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        return Response.ok(Map.of(
            "status", "UP",
            "service", "Discord Bot API",
            "timestamp", System.currentTimeMillis()
        )).build();
    }
}