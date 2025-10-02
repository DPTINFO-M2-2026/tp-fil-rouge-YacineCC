package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.Role;
import fr.univtln.yhaouas846.projet.entity.User;
import fr.univtln.yhaouas846.projet.entity.Guild;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
    public Response getRolesByGuild(@PathParam("guildId") Long guildId) {
        Guild guild = Guild.findById(guildId);
        if (guild == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<Role> roles = Role.find("guild", guild).list();
        return Response.ok(roles).build();
    }

    @POST
    @Path("/{roleId}/users/{userId}")
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

    @DELETE
    @Path("/{roleId}/users/{userId}")
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