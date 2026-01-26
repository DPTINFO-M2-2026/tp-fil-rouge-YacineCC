package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Channel;
import fr.univtln.yhaouas846.projet.entity.Guild;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST pour gérer les canaux (channels) d'une guilde.
 *
 * <p>Cette API expose un CRUD simple via Panache, et des endpoints de filtrage
 * par guilde et par type de canal.</p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/channels} : liste tous les canaux</li>
 *   <li>{@code GET /api/channels/{id}} : récupère un canal par identifiant</li>
 *   <li>{@code POST /api/channels} : crée un canal (ou met à jour si {@code discordId} existe déjà)</li>
 *   <li>{@code PUT /api/channels/{id}} : met à jour un canal existant</li>
 *   <li>{@code DELETE /api/channels/{id}} : supprime un canal</li>
 *   <li>{@code GET /api/channels/guild/{guildId}} : liste les canaux d'une guilde</li>
 *   <li>{@code GET /api/channels/type/{type}} : liste les canaux d'un type donné</li>
 * </ul>
 */
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

    /**
     * Crée un canal.
     *
     * <p>Si {@code discordId} est fourni et déjà présent, l'entité existante est mise à jour
     * afin de supporter un mode de synchronisation Discord → base de données.</p>
     *
     * @param channel canal à créer/mettre à jour
     * @return {@code 201} si création, {@code 200} si mise à jour, {@code 400} sinon
     */
    @POST
    @Transactional
    public Response createChannel(@Valid Channel channel) {
        try {
            // Check if channel already exists by discordId
            if (channel.discordId != null) {
                Channel existingChannel = Channel.find("discordId", channel.discordId).firstResult();
                if (existingChannel != null) {
                    // Update existing channel
                    existingChannel.name = channel.name;
                    existingChannel.description = channel.description;
                    existingChannel.type = channel.type;
                    existingChannel.position = channel.position;
                    existingChannel.isNsfw = channel.isNsfw;
                    existingChannel.guild = channel.guild;
                    existingChannel.persist();
                    return Response.ok(existingChannel).build();
                }
            }

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
    /**
     * Retourne tous les canaux appartenant à une guilde.
     *
     * @param guildId identifiant de la guilde
     * @return {@code 200} avec la liste, {@code 404} si la guilde n'existe pas
     */
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
    /**
     * Filtre les canaux par type.
     *
     * @param type type de canal (enum)
     * @return liste des canaux correspondant
     */
    public List<Channel> getChannelsByType(@PathParam("type") Channel.ChannelType type) {
        return Channel.find("type", type).list();
    }
}