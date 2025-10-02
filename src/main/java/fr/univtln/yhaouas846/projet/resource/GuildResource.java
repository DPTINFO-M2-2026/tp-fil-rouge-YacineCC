package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/guilds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuildResource {

    @GET
    public List<Guild> getAllGuilds() {
        return Guild.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getGuildById(@PathParam("id") Long id) {
        Guild guild = Guild.findById(id);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(guild).build();
    }

    @POST
    public Response createGuild(@Valid Guild guild) {
        try {
            guild.persist();
            return Response.status(Response.Status.CREATED).entity(guild).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating guild: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateGuild(@PathParam("id") Long id, @Valid Guild updatedGuild) {
        Guild guild = Guild.findById(id);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        guild.name = updatedGuild.name;
        guild.description = updatedGuild.description;
        guild.iconUrl = updatedGuild.iconUrl;
        guild.memberLimit = updatedGuild.memberLimit;
        
        try {
            guild.persist();
            return Response.ok(guild).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating guild: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGuild(@PathParam("id") Long id) {
        Guild guild = Guild.findById(id);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        guild.delete();
        return Response.noContent().build();
    }

    @POST
    @Path("/{guildId}/members/{userId}")
    public Response addMemberToGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        Guild guild = Guild.findById(guildId);
        User user = User.findById(userId);
        
        if (guild == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        guild.members.add(user);
        guild.persist();
        
        return Response.ok().build();
    }

    @DELETE
    @Path("/{guildId}/members/{userId}")
    public Response removeMemberFromGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        Guild guild = Guild.findById(guildId);
        User user = User.findById(userId);
        
        if (guild == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        guild.members.remove(user);
        guild.persist();
        
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/members")
    public Response getGuildMembers(@PathParam("id") Long id) {
        Guild guild = Guild.findById(id);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(guild.members).build();
    }
}