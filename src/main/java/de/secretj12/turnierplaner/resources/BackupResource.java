package de.secretj12.turnierplaner.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.secretj12.turnierplaner.db.repositories.*;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/backup")
@RolesAllowed("admin")
public class BackupResource {

    @Inject
    TournamentRepository tournamentRepository;
    @Inject
    CompetitionRepository competitionRepository;
    @Inject
    MatchRepository matchRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    CourtRepositiory courtRepository;
    @Inject
    GroupRepository groupRepository;
    @Inject
    TeamRepository teamRepository;
    @Inject
    SetRepository setRepository;
    @Inject
    NextMatchRepository nextMatchRepository;
    @Inject
    MatchOfGroupRepository matchOfGroupRepository;
    @Inject
    FinalOfGroupRepository finalOfGroupRepository;
    @Inject
    ConfigRepository configRepository;
    @Inject
    DefaultConfigRepository defaultConfigRepository;

    private final ObjectMapper objectMapper;

    public BackupResource() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private void dumpTable(ZipOutputStream zos, String tableName, PanacheRepository<?> repository) throws IOException {
        zos.putNextEntry(new ZipEntry(tableName + ".json"));
        zos.write(objectMapper.writeValueAsBytes(repository.listAll()));
        zos.closeEntry();
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        // General hierarchy
        dumpTable(zos, "tournaments", tournamentRepository);
        dumpTable(zos, "competitions", competitionRepository);
        dumpTable(zos, "matches", matchRepository);

        // Players
        dumpTable(zos, "players", playerRepository);
        dumpTable(zos, "teams", teamRepository);

        // Courts
        dumpTable(zos, "courts", courtRepository);

        // Groups
        dumpTable(zos, "groups", groupRepository);
        dumpTable(zos, "matchesOfGroup", matchOfGroupRepository);
        dumpTable(zos, "finalsOfGroup", finalOfGroupRepository);
        // Knockout
        dumpTable(zos, "nextMatches", nextMatchRepository);
        dumpTable(zos, "sets", setRepository);

        // Configs
        dumpTable(zos, "config", configRepository);
        dumpTable(zos, "defaultConfig", defaultConfigRepository);

        zos.finish();

        String filename = "turnierplaner_" + LocalDate.now() + ".turnier";

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Response.ok(baos.toByteArray())
                       .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                       .build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    public Response upload(byte[] data) {
        tournamentRepository.deleteAll();
        playerRepository.deleteAll();
        configRepository.deleteAll();
        defaultConfigRepository.deleteAll();
        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        return Response.ok().build();
    }
}
