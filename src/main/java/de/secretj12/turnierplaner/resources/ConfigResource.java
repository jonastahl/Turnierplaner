package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Config;
import de.secretj12.turnierplaner.db.entities.DefaultConfig;
import de.secretj12.turnierplaner.db.repositories.ConfigRepository;
import de.secretj12.turnierplaner.db.repositories.DefaultConfigRepository;
import de.secretj12.turnierplaner.model.user.jUserConfig;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/config")
public class ConfigResource {

    @Inject
    DefaultConfigRepository defaultConfigRepository;
    @Inject
    ConfigRepository configRepository;

    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "turnierplaner.frontend.oidc")
    String frontendOidc;
    @ConfigProperty(name = "turnierplaner.frontend.legal_notice")
    String legalNotice;
    @ConfigProperty(name = "turnierplaner.frontend.privacy_policy")
    String privacyPolicy;

    @GET
    @Path("/load")
    @Produces(MediaType.APPLICATION_JSON)
    public jUserConfig loadUserConfig() {
        Config config = null;
        if (jwt.getSubject() != null)
            config = configRepository.findByUUID(UUID.fromString(jwt.getSubject()));
        return new jUserConfig(defaultConfigRepository.findById(0L), config, frontendOidc, legalNotice, privacyPolicy);
    }

    @POST
    @Path("/save")
    @Transactional
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveConfig(jUserConfig nConfig) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        DefaultConfig defConfig = defaultConfigRepository.findById(0L);
        Config config = configRepository.findByUUID(uuid);
        if (config == null) {
            config = new Config();
            config.setId(uuid);
            config.setLanguage(defConfig.getLanguage());
        }
        if (nConfig.getLanguage() != null)
            config.setLanguage(nConfig.getLanguage());
        configRepository.persist(config);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/saveDefaultLanguage")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveDefaultLanguage(jUserConfig nConfig) {
        DefaultConfig defConfig = defaultConfigRepository.findById(0L);
        defConfig.setLanguage(nConfig.getLanguage());
        defaultConfigRepository.persist(defConfig);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/saveTitle")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveTitle(jUserConfig nConfig) {
        DefaultConfig defConfig = defaultConfigRepository.findById(0L);
        if (nConfig.getTitle() != null)
            defConfig.setTitle(nConfig.getTitle());
        else
            defConfig.setTitle("title");

        defaultConfigRepository.persist(defConfig);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/saveIsAdminNeeded")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveIsAdminNeeded(jUserConfig nConfig) {
        DefaultConfig defConfig = defaultConfigRepository.findById(0L);
        defConfig.setAdminVerificationNeeded(nConfig.isAdminVerificationNeeded());
        defaultConfigRepository.persist(defConfig);
    }

    @GET
    @Path("/isAdmin")
    public boolean isAdmin() {
        return securityIdentity.hasRole("admin");
    }

    @GET
    @Path("/isDirector")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean isDirector() {
        return securityIdentity.hasRole("director");
    }

    @GET
    @Path("/isReporter")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean isReporter() {
        return securityIdentity.hasRole("reporter");
    }
}
