package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Role;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.entity.Guild;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST de gestion des rôles (permissions) au sein d'une guilde.
 *
 * <p>Les rôles sont modélisés par l'entité {@link fr.univtln.yhaouas846.projet.entity.Role}.
 * En plus des opérations CRUD, cette ressource expose des endpoints pour affecter/retirer
 * un rôle à un utilisateur (via l'association {@code user_roles}).</p>
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

    @GET
    public List<Role> getAllRoles() {
        return Role.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getRoleById(@PathParam("id") Long id) {
        Role role = Role.findById(id);
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(role).build();
    }

    @POST
    @Transactional
    public Response createRole(@Valid Role role) {
        try {
            role.persist();
            return Response.status(Response.Status.CREATED).entity(role).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating role: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateRole(@PathParam("id") Long id, @Valid Role updatedRole) {
        Role role = Role.findById(id);
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        role.name = updatedRole.name;
        role.color = updatedRole.color;
        role.position = updatedRole.position;
        role.canManageChannels = updatedRole.canManageChannels;
        role.canManageRoles = updatedRole.canManageRoles;
        role.canManageMessages = updatedRole.canManageMessages;
        role.canKickMembers = updatedRole.canKickMembers;
        role.canBanMembers = updatedRole.canBanMembers;
        role.canSendMessages = updatedRole.canSendMessages;
        role.canReadMessages = updatedRole.canReadMessages;
        
        try {
            role.persist();
            return Response.ok(role).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating role: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteRole(@PathParam("id") Long id) {
        Role role = Role.findById(id);
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        role.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/guild/{guildId}")
    /**
     * Liste les rôles d'une guilde.
     *
     * @param guildId identifiant de la guilde
     * @return {@code 200} avec la liste ou {@code 404} si la guilde est inconnue
     */
    public Response getRolesByGuild(@PathParam("guildId") Long guildId) {
        Guild guild = Guild.findById(guildId);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<Role> roles = Role.find("guild", guild).list();
        return Response.ok(roles).build();
    }

    /**
     * Assigne un rôle à un utilisateur.
     *
     * <p>Cette opération ajoute l'utilisateur à l'ensemble {@link fr.univtln.yhaouas846.projet.entity.Role#users}
     * puis persiste le rôle. La cohérence JPA dépend de la configuration de la relation et du contexte
     * transactionnel.</p>
     *
     * @param roleId identifiant du rôle
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès, {@code 404} si rôle ou utilisateur absent
     */
    @POST
    @Path("/{roleId}/users/{userId}")
    @Transactional
    public Response assignRoleToUser(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
        Role role = Role.findById(roleId);
        User user = User.findById(userId);
        
        if (role == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        role.users.add(user);
        role.persist();
        
        return Response.ok().build();
    }

    /**
     * Retire un rôle à un utilisateur.
     *
     * @param roleId identifiant du rôle
     * @param userId identifiant de l'utilisateur
     * @return {@code 200} si succès, {@code 404} si rôle ou utilisateur absent
     */
    @DELETE
    @Path("/{roleId}/users/{userId}")
    @Transactional
    public Response removeRoleFromUser(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
        Role role = Role.findById(roleId);
        User user = User.findById(userId);
        
        if (role == null || user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        role.users.remove(user);
        role.persist();
        
        return Response.ok().build();
    }
}