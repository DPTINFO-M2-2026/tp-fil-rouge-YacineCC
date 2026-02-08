package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.service.RoleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST de gestion des rôles (permissions) au sein d'une guilde.
 *
 * <p>Cette ressource délègue la logique métier au {@link RoleService}
 * et utilise des DTOs pour le contrat API.</p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/roles}</li>
 *   <li>{@code GET /api/roles/{id}}</li>
 *   <li>{@code POST /api/roles}</li>
 *   <li>{@code PUT /api/roles/{id}}</li>
 *   <li>{@code DELETE /api/roles/{id}}</li>
 *   <li>{@code GET /api/roles/guild/{guildId}} : rôles d'une guilde</li>
 *   <li>{@code POST /api/roles/{roleId}/users/{userId}} : assigne un rôle</li>
 *   <li>{@code DELETE /api/roles/{roleId}/users/{userId}} : retire un rôle</li>
 * </ul>
 */
@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    RoleService roleService;

    /**
     * Liste tous les rôles.
     */
    @GET
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Récupère un rôle par son identifiant.
     */
    @GET
    @Path("/{id}")
    public RoleDTO getRoleById(@PathParam("id") Long id) {
        return roleService.getRoleById(id);
    }

    /**
     * Crée un rôle.
     *
     * @param dto données de création
     * @return {@code 201} avec le DTO du rôle créé
     */
    @POST
    public Response createRole(@Valid CreateRoleDTO dto) {
        RoleDTO created = roleService.createRole(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Met à jour un rôle existant.
     */
    @PUT
    @Path("/{id}")
    public RoleDTO updateRole(@PathParam("id") Long id, @Valid CreateRoleDTO dto) {
        return roleService.updateRole(id, dto);
    }

    /**
     * Supprime un rôle.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteRole(@PathParam("id") Long id) {
        roleService.deleteRole(id);
        return Response.noContent().build();
    }

    /**
     * Liste les rôles d'une guilde.
     *
     * @param guildId identifiant de la guilde
     * @return liste des rôles de la guilde
     */
    @GET
    @Path("/guild/{guildId}")
    public List<RoleDTO> getRolesByGuild(@PathParam("guildId") Long guildId) {
        return roleService.getRolesByGuild(guildId);
    }

    /**
     * Assigne un rôle à un utilisateur.
     *
     * @param roleId identifiant du rôle
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès
     */
    @POST
    @Path("/{roleId}/users/{userId}")
    @Consumes(MediaType.WILDCARD)
    public Response assignRoleToUser(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
        roleService.assignRoleToUser(roleId, userId);
        return Response.ok().build();
    }

    /**
     * Retire un rôle à un utilisateur.
     *
     * @param roleId identifiant du rôle
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès
     */
    @DELETE
    @Path("/{roleId}/users/{userId}")
    @Consumes(MediaType.WILDCARD)
    public Response removeRoleFromUser(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
        roleService.removeRoleFromUser(roleId, userId);
        return Response.ok().build();
    }
}