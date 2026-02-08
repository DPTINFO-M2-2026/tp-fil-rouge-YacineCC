package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.service.GuildService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * Ressource REST de gestion des guildes (serveurs).
 *
 * <p>Cette ressource délègue la logique métier au {@link GuildService}
 * et utilise des DTOs pour le contrat API.</p>
 *
 * <p>Elle propose un CRUD et des opérations simples pour gérer l'appartenance
 * d'un utilisateur à une guilde (ajout/retrait/listage).</p>
 */
@Path("/api/guilds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuildResource {

    @Inject
    GuildService guildService;

    /**
     * Liste toutes les guildes (version résumée).
     */
    @GET
    public List<GuildSummaryDTO> getAllGuilds() {
        return guildService.getAllGuilds();
    }

    /**
     * Récupère une guilde par son identifiant.
     */
    @GET
    @Path("/{id}")
    public GuildDTO getGuildById(@PathParam("id") Long id) {
        return guildService.getGuildById(id);
    }

    /**
     * Crée une guilde.
     *
     * <p>Si {@code discordId} est fourni et correspond déjà à une guilde en base, la guilde existante
     * est mise à jour (comportement utile lors d'une synchronisation).</p>
     *
     * @param dto données de création
     * @return {@code 201} avec le DTO de la guilde créée
     */
    @POST
    public Response createGuild(@Valid CreateGuildDTO dto) {
        GuildDTO created = guildService.createGuild(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Met à jour une guilde existante (mise à jour partielle).
     */
    @PUT
    @Path("/{id}")
    public GuildDTO updateGuild(@PathParam("id") Long id, @Valid UpdateGuildDTO dto) {
        return guildService.updateGuild(id, dto);
    }

    /**
     * Supprime une guilde.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteGuild(@PathParam("id") Long id) {
        guildService.deleteGuild(id);
        return Response.noContent().build();
    }

    /**
     * Ajoute un membre à une guilde.
     *
     * @param guildId identifiant de la guilde
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès
     */
    @POST
    @Path("/{guildId}/members/{userId}")
    @Consumes(MediaType.WILDCARD)
    public Response addMemberToGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        guildService.addMember(guildId, userId);
        return Response.ok().build();
    }

    /**
     * Retire un membre d'une guilde.
     *
     * @param guildId identifiant de la guilde
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès
     */
    @DELETE
    @Path("/{guildId}/members/{userId}")
    @Consumes(MediaType.WILDCARD)
    public Response removeMemberFromGuild(@PathParam("guildId") Long guildId, @PathParam("userId") Long userId) {
        guildService.removeMember(guildId, userId);
        return Response.ok().build();
    }

    /**
     * Liste les membres d'une guilde.
     *
     * @param id identifiant de la guilde
     * @return ensemble des membres (résumé)
     */
    @GET
    @Path("/{id}/members")
    public Set<UserSummaryDTO> getGuildMembers(@PathParam("id") Long id) {
        return guildService.getMembers(id);
    }
}