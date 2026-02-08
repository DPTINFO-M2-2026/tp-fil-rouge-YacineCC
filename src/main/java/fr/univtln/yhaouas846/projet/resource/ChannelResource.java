package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.entity.Channel.ChannelType;
import fr.univtln.yhaouas846.projet.service.ChannelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST pour gérer les canaux (channels) d'une guilde.
 *
 * <p>Cette ressource délègue la logique métier au {@link ChannelService}
 * et utilise des DTOs pour le contrat API.</p>
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

    @Inject
    ChannelService channelService;

    /**
     * Liste tous les canaux.
     */
    @GET
    public List<ChannelDTO> getAllChannels() {
        return channelService.getAllChannels();
    }

    /**
     * Récupère un canal par son identifiant.
     */
    @GET
    @Path("/{id}")
    public ChannelDTO getChannelById(@PathParam("id") Long id) {
        return channelService.getChannelById(id);
    }

    /**
     * Crée un canal.
     *
     * <p>Si {@code discordId} est fourni et déjà présent, l'entité existante est mise à jour
     * afin de supporter un mode de synchronisation Discord → base de données.</p>
     *
     * @param dto données de création
     * @return {@code 201} avec le DTO du canal créé
     */
    @POST
    public Response createChannel(@Valid CreateChannelDTO dto) {
        ChannelDTO created = channelService.createChannel(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Met à jour un canal existant.
     */
    @PUT
    @Path("/{id}")
    public ChannelDTO updateChannel(@PathParam("id") Long id, @Valid CreateChannelDTO dto) {
        return channelService.updateChannel(id, dto);
    }

    /**
     * Supprime un canal.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteChannel(@PathParam("id") Long id) {
        channelService.deleteChannel(id);
        return Response.noContent().build();
    }

    /**
     * Retourne tous les canaux appartenant à une guilde.
     *
     * @param guildId identifiant de la guilde
     * @return liste des canaux de la guilde
     */
    @GET
    @Path("/guild/{guildId}")
    public List<ChannelDTO> getChannelsByGuild(@PathParam("guildId") Long guildId) {
        return channelService.getChannelsByGuild(guildId);
    }

    /**
     * Filtre les canaux par type.
     *
     * @param type type de canal (enum)
     * @return liste des canaux correspondant
     */
    @GET
    @Path("/type/{type}")
    public List<ChannelDTO> getChannelsByType(@PathParam("type") ChannelType type) {
        return channelService.getChannelsByType(type);
    }
}