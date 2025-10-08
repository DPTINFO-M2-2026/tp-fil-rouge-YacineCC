package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Guild;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChannelResource {

    @GET
    public List<Channel> getAllChannels() {
        return Channel.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getChannelById(@PathParam("id") Long id) {
        Channel channel = Channel.findById(id);
        if (channel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(channel).build();
    }

    @POST
    @Transactional
    public Response createChannel(@Valid Channel channel) {
        try {
            channel.persist();
            return Response.status(Response.Status.CREATED).entity(channel).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating channel: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateChannel(@PathParam("id") Long id, @Valid Channel updatedChannel) {
        Channel channel = Channel.findById(id);
        if (channel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        channel.name = updatedChannel.name;
        channel.description = updatedChannel.description;
        channel.type = updatedChannel.type;
        channel.position = updatedChannel.position;
        channel.isNsfw = updatedChannel.isNsfw;
        
        try {
            channel.persist();
            return Response.ok(channel).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating channel: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteChannel(@PathParam("id") Long id) {
        Channel channel = Channel.findById(id);
        if (channel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        channel.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/guild/{guildId}")
    public Response getChannelsByGuild(@PathParam("guildId") Long guildId) {
        Guild guild = Guild.findById(guildId);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<Channel> channels = Channel.find("guild", guild).list();
        return Response.ok(channels).build();
    }

    @GET
    @Path("/type/{type}")
    public List<Channel> getChannelsByType(@PathParam("type") Channel.ChannelType type) {
        return Channel.find("type", type).list();
    }
}