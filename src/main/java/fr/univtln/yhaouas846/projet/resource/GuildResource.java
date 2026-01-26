package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Guild;
import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST de gestion des guildes (serveurs).
 *
 * <p>Une guilde est représentée par l'entité {@link fr.univtln.yhaouas846.projet.entity.Guild}.
 * Cette ressource propose un CRUD et des opérations simples pour gérer l'appartenance
 * d'un utilisateur à une guilde (ajout/retrait/listage).</p>
 *
 * <p>Les endpoints d'écriture sont transactionnels. Les payloads JSON sont validés via
 * {@link jakarta.validation.Valid}.</p>
 */
@Path("/api/guilds")
@Produces(MediaType.APPLICATION_JSON)
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
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    /**
     * Crée une guilde.
     *
     * <p>Si {@code discordId} est fourni et correspond déjà à une guilde en base, la guilde existante
     * est mise à jour (comportement utile lors d'une synchronisation).</p>
     *
     * @param guild guilde à créer/mettre à jour
     * @return {@code 201} si création, {@code 200} si mise à jour, {@code 400} sinon
     */
    public Response createGuild(@Valid Guild guild) {
        try {
            // Check if guild already exists by discordId
            if (guild.discordId != null) {
                Guild existingGuild = Guild.find("discordId", guild.discordId).firstResult();
                if (existingGuild != null) {
                    // Update existing guild
                    existingGuild.name = guild.name;
                    existingGuild.description = guild.description;
                    existingGuild.iconUrl = guild.iconUrl;
                    existingGuild.memberLimit = guild.memberLimit;
                    existingGuild.owner = guild.owner; // Update owner if changed
                    existingGuild.persist();
                    return Response.ok(existingGuild).build();
                }
            }

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
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Transactional
    public Response deleteGuild(@PathParam("id") Long id) {
        Guild guild = Guild.findById(id);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        guild.delete();
        return Response.noContent().build();
    }

    /**
     * Ajoute un membre à une guilde.
     *
     * <p>Cette méthode initialise l'ensemble {@code guild.members} si nécessaire, ajoute l'utilisateur
     * puis persiste la guilde.</p>
     *
     * @param guildId identifiant de la guilde
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès, {@code 404} si guilde ou utilisateur introuvable
     */
    @POST
    @Path("/{guildId}/members/{userId}")
    @Transactional
    public Response addMemberToGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        Guild guild = Guild.findById(guildId);
        User user = User.findById(userId);
        
        if (guild == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        if (guild.members == null) {
            guild.members = new java.util.HashSet<>();
        }
        guild.members.add(user);
        guild.persist();
        
        return Response.ok().build();
    }

    /**
     * Retire un membre d'une guilde.
     *
     * @param guildId identifiant de la guilde
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès, {@code 404} si guilde ou utilisateur introuvable
     */
    @DELETE
    @Path("/{guildId}/members/{userId}")
    @Transactional
    public Response removeMemberFromGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        Guild guild = Guild.findById(guildId);
        User user = User.findById(userId);
        
        if (guild == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        if (guild.members != null) {
            guild.members.remove(user);
        }
        guild.persist();
        
        return Response.ok().build();
    }

    /**
     * Liste les membres d'une guilde.
     *
     * @param id identifiant de la guilde
     * @return {@code 200} avec l'ensemble des membres, {@code 404} si la guilde n'existe pas
     */
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