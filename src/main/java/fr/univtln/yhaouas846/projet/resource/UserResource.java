package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST exposant des opérations CRUD sur les utilisateurs.
 *
 * <p>Cette ressource s'appuie sur Panache (méthodes statiques) pour accéder aux entités
 * {@link fr.univtln.yhaouas846.projet.entity.User}.</p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/users} : liste tous les utilisateurs</li>
 *   <li>{@code GET /api/users/{id}} : récupère un utilisateur par identifiant</li>
 *   <li>{@code POST /api/users} : crée un utilisateur (ou met à jour si {@code discordId} existe déjà)</li>
 *   <li>{@code PUT /api/users/{id}} : met à jour un utilisateur existant</li>
 *   <li>{@code DELETE /api/users/{id}} : supprime un utilisateur</li>
 *   <li>{@code GET /api/users/username/{username}} : recherche par nom d'utilisateur</li>
 *   <li>{@code GET /api/users/bots} : liste des comptes bot</li>
 * </ul>
 *
 * <p>Les opérations d'écriture sont annotées {@link jakarta.transaction.Transactional}.
 * Les payloads {@code POST}/{@code PUT} sont validés via {@link jakarta.validation.Valid}.</p>
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public List<User> getAllUsers() {
        return User.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    /**
     * Crée un utilisateur.
     *
     * <p>Si le payload contient un {@code discordId} déjà présent en base, l'utilisateur existant
     * est mis à jour (comportement idempotent côté synchronisation Discord). Sinon, une nouvelle
     * ligne est créée.</p>
     *
     * @param user représentation de l'utilisateur à créer/mettre à jour
     * @return {@code 201} si création, {@code 200} si mise à jour, ou {@code 400} en cas d'erreur
     */
    @POST
    @Transactional
    public Response createUser(@Valid User user) {
        try {
            // Check if user already exists by discordId
            if (user.discordId != null) {
                User existingUser = User.find("discordId", user.discordId).firstResult();
                if (existingUser != null) {
                    // Update existing user
                    existingUser.username = user.username;
                    existingUser.discriminator = user.discriminator;
                    existingUser.avatarUrl = user.avatarUrl;
                    existingUser.isBot = user.isBot;
                    existingUser.persist();
                    return Response.ok(existingUser).build();
                }
            }
            
            user.persist();
            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating user: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param id identifiant de l'utilisateur
     * @param updatedUser nouvelles valeurs
     * @return {@code 200} si succès, {@code 404} si introuvable, {@code 400} si validation/échec
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, @Valid User updatedUser) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        user.username = updatedUser.username;
        user.discriminator = updatedUser.discriminator;
        user.email = updatedUser.email;
        user.avatarUrl = updatedUser.avatarUrl;
        user.isBot = updatedUser.isBot;
        
        try {
            user.persist();
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating user: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Supprime un utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @return {@code 204} si suppression, {@code 404} si introuvable
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        user.delete();
        return Response.noContent().build();
    }

    /**
     * Recherche un utilisateur par son {@code username}.
     *
     * @param username nom d'utilisateur
     * @return {@code 200} si trouvé, {@code 404} sinon
     */
    @GET
    @Path("/username/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        User user = User.find("username", username).firstResult();
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    @GET
    @Path("/bots")
    public List<User> getBots() {
        return User.find("isBot", true).list();
    }
}