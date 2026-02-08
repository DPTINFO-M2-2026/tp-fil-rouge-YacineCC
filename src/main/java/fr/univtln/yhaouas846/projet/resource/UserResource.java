package fr.univtln.yhaouas846.projet.resource;

import fr.univtln.yhaouas846.projet.dto.*;
import fr.univtln.yhaouas846.projet.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Ressource REST exposant des opérations CRUD sur les utilisateurs.
 *
 * <p>Cette ressource délègue la logique métier au {@link UserService}
 * et utilise des DTOs pour le contrat API.</p>
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
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    /**
     * Liste tous les utilisateurs.
     *
     * @return liste de tous les utilisateurs enregistrés (résumée)
     */
    @GET
    public List<UserSummaryDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return DTO complet de l'utilisateur
     */
    @GET
    @Path("/{id}")
    public UserDTO getUserById(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    /**
     * Crée un utilisateur.
     *
     * <p>Si le payload contient un {@code discordId} déjà présent en base, l'utilisateur existant
     * est mis à jour (comportement idempotent côté synchronisation Discord).</p>
     *
     * @param dto données de création
     * @return {@code 201} avec le DTO de l'utilisateur créé
     */
    @POST
    public Response createUser(@Valid CreateUserDTO dto) {
        UserDTO created = userService.createUser(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param id identifiant de l'utilisateur
     * @param dto nouvelles valeurs (mise à jour partielle)
     * @return DTO mis à jour
     */
    @PUT
    @Path("/{id}")
    public UserDTO updateUser(@PathParam("id") Long id, @Valid UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    /**
     * Supprime un utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @return {@code 204} si suppression réussie
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

    /**
     * Recherche un utilisateur par son {@code username}.
     *
     * @param username nom d'utilisateur
     * @return DTO complet de l'utilisateur
     */
    @GET
    @Path("/username/{username}")
    public UserDTO getUserByUsername(@PathParam("username") String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * Liste tous les comptes bot.
     *
     * @return liste résumée des utilisateurs bots
     */
    @GET
    @Path("/bots")
    public List<UserSummaryDTO> getBots() {
        return userService.getBotUsers();
    }
}