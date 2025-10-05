package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.enums.AdminRole;
import de.secretj12.turnierplaner.model.admin.jAdminUser;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Path("/user")
@RolesAllowed("admin")
public class UserResource {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "turnierplaner.keycloak.realm")
    String realm;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/list")
    public List<jAdminUser> list() {
        return keycloak.realm(realm).users().list().stream()
            .filter(u -> !u.getUsername().equals(securityIdentity.getPrincipal().getName()))
            .map(u -> new jAdminUser(u, getRole(u)))
            .toList();
    }

    @POST
    @Path("/updateUser")
    public String updateUser(jAdminUser user) {
        var kUser = keycloak.realm(realm).users().get(user.getId());
        var oldGroup = keycloak.realm(realm).getGroupByPath(getRole(kUser).label);
        var newGroup = keycloak.realm(realm).getGroupByPath(user.getRole().label);
        kUser.leaveGroup(oldGroup.getId());
        kUser.joinGroup(newGroup.getId());
        return "User updated";
    }

    private AdminRole getRole(UserRepresentation user) {
        return getRole(keycloak.realm(realm).users().get(user.getId()));
    }

    private AdminRole getRole(org.keycloak.admin.client.resource.UserResource user) {
        var groups = user.groups().stream().map(GroupRepresentation::getName).toList();
        if (groups.contains("admin"))
            return AdminRole.ADMIN;
        else if (groups.contains("director"))
            return AdminRole.DIRECTOR;
        else if (groups.contains("reporter"))
            return AdminRole.REPORTER;
        else if (groups.contains("user"))
            return AdminRole.USER;
        else
            throw new InternalServerErrorException("Invalid role");
    }
}
