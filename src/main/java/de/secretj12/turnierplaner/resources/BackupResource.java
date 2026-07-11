package de.secretj12.turnierplaner.resources;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.secretj12.turnierplaner.cache.BackupDownloadTokenCache;
import de.secretj12.turnierplaner.db.entities.*;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.enums.*;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.quarkus.hibernate.orm.panache.Panache.getEntityManager;

@Path("/backup")
public class BackupResource {
    private static final Logger log = LoggerFactory.getLogger(BackupResource.class);

    @Inject
    BackupDownloadTokenCache backupDownloadTokenCache;

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

    private LocalDate parseLocalDate(JsonNode node) {
        if (node.isArray()) {
            int year = node.get(0).asInt();
            int month = node.get(1).asInt();
            int day = node.get(2).asInt();
            return LocalDate.of(year, month, day);
        }
        return LocalDate.parse(node.asText());
    }

    private Instant parseInstant(JsonNode node) {
        if (node.isNumber()) {
            long seconds = node.decimalValue().longValue();
            int nanos = node.decimalValue().remainder(java.math.BigDecimal.ONE)
                .movePointRight(9).intValue();
            return Instant.ofEpochSecond(seconds, nanos);
        }
        return Instant.parse(node.asText());
    }

    private UUID getUUID(JsonNode node, String field) {
        return node.hasNonNull(field) ? UUID.fromString(node.get(field).asText()) : null;
    }

    private String getText(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private void restoreConfig(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO config (id, language) VALUES (:id, :language)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("language", getText(node, "language"))
                .executeUpdate();
        }
    }

    private void restoreDefaultConfig(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO default_config (id, title, language, adminVerificationNeeded) VALUES (:id, :title, :language, :adminVerificationNeeded)")
                .setParameter("id", node.hasNonNull("id") ? node.get("id").asInt() : null)
                .setParameter("title", getText(node, "title"))
                .setParameter("language", getText(node, "language"))
                .setParameter("adminVerificationNeeded", node.hasNonNull("adminVerificationNeeded") ? node.get(
                    "adminVerificationNeeded").asBoolean() : null)
                .executeUpdate();
        }
    }

    private void restoreCourts(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO courts (name, courtType) VALUES (:name, :courtType)")
                .setParameter("name", getText(node, "name"))
                .setParameter("courtType", node.hasNonNull("courtType") ? CourtType.valueOf(node.get("courtType")
                    .asText()).ordinal() : null)
                .executeUpdate();
        }
    }

    private void restorePlayers(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO players (id, first_name, last_name, sex, birthday, email, mail_verified, admin_verified, language) " + "VALUES (:id, :firstName, :lastName, :sex, :birthday, :email, :mailVerified, :adminVerified, :language)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("firstName", getText(node, "firstName"))
                .setParameter("lastName", getText(node, "lastName"))
                .setParameter("sex", node.hasNonNull("sex") ? Sex.valueOf(node.get("sex").asText()).ordinal() : null)
                .setParameter("birthday", node.hasNonNull("birthday") ? parseLocalDate(node.get("birthday")) : null)
                .setParameter("email", getText(node, "email"))
                .setParameter("mailVerified", node.hasNonNull("mailVerified") ? node.get("mailVerified")
                    .asBoolean() : null)
                .setParameter("adminVerified", node.hasNonNull("adminVerified") ? node.get("adminVerified")
                    .asBoolean() : null)
                .setParameter("language", node.hasNonNull("language") ? Language.valueOf(node.get("language").asText())
                    .ordinal() : null)
                .executeUpdate();
        }
    }

    private void restoreTournaments(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            UUID tournamentId = getUUID(node, "id");
            getEntityManager().createNativeQuery(
                "INSERT INTO tournaments (id, name, description, begin_registration, end_registration, begin_game_phase, end_game_phase, visible) " + "VALUES (:id, :name, :description, :beginRegistration, :endRegistration, :beginGamePhase, :endGamePhase, :visible)")
                .setParameter("id", tournamentId)
                .setParameter("name", getText(node, "name"))
                .setParameter("description", getText(node, "description"))
                .setParameter("beginRegistration", node.hasNonNull("beginRegistration") ? java.sql.Timestamp.from(
                    parseInstant(node.get("beginRegistration"))) : null)
                .setParameter("endRegistration", node.hasNonNull("endRegistration") ? java.sql.Timestamp.from(
                    parseInstant(node.get("endRegistration"))) : null)
                .setParameter("beginGamePhase", node.hasNonNull("beginGamePhase") ? java.sql.Timestamp.from(
                    parseInstant(node.get("beginGamePhase"))) : null)
                .setParameter("endGamePhase", node.hasNonNull("endGamePhase") ? java.sql.Timestamp.from(parseInstant(
                    node.get("endGamePhase"))) : null)
                .setParameter("visible", node.hasNonNull("visible") ? node.get("visible").asBoolean() : null)
                .executeUpdate();
            if (node.hasNonNull("courts")) {
                for (JsonNode courtNode : node.get("courts")) {
                    getEntityManager().createNativeQuery(
                        "INSERT INTO court_of_tournament (id, name) VALUES (:id, :name)")
                        .setParameter("id", tournamentId)
                        .setParameter("name", courtNode.asText())
                        .executeUpdate();
                }
            }
        }
    }

    private void restoreCompetitions(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO competitions (id, name, description, type, mode, signup, number_sets, " + "playerA_sex, playerA_has_min_age, playerA_min_age, playerA_has_max_age, playerA_max_age, " + "playerB_different, playerB_sex, playerB_has_min_age, playerB_min_age, playerB_has_max_age, playerB_max_age, " + "creation_progess, totalRounds, tournament_id, finale_id, thirdPlace_id) " + "VALUES (:id, :name, :description, :type, :mode, :signup, :numberSets, " + ":playerASex, :playerAhasMinAge, :playerAminAge, :playerAhasMaxAge, :playerAmaxAge, " + ":playerBdifferent, :playerBSex, :playerBhasMinAge, :playerBminAge, :playerBhasMaxAge, :playerBmaxAge, " + ":cProgress, :totalRounds, :tournamentId, :finaleId, :thirdPlaceId)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("name", getText(node, "name"))
                .setParameter("description", getText(node, "description"))
                .setParameter("type", node.hasNonNull("type") ? CompetitionType.valueOf(node.get("type").asText())
                    .ordinal() : null)
                .setParameter("mode", node.hasNonNull("mode") ? CompetitionMode.valueOf(node.get("mode").asText())
                    .ordinal() : null)
                .setParameter("signup", node.hasNonNull("signup") ? CompetitionSignUp.valueOf(node.get("signup")
                    .asText()).ordinal() : null)
                .setParameter("numberSets", node.hasNonNull("numberSets") ? NumberSets.valueOf(node.get("numberSets")
                    .asText()).ordinal() : null)
                .setParameter("playerASex", node.hasNonNull("playerASex") ? SexFilter.valueOf(node.get("playerASex")
                    .asText()).ordinal() : null)
                .setParameter("playerAhasMinAge", node.hasNonNull("playerAhasMinAge") ? node.get("playerAhasMinAge")
                    .asBoolean() : null)
                .setParameter("playerAminAge", node.hasNonNull("playerAminAge") ? parseLocalDate(node.get(
                    "playerAminAge")) : null)
                .setParameter("playerAhasMaxAge", node.hasNonNull("playerAhasMaxAge") ? node.get("playerAhasMaxAge")
                    .asBoolean() : null)
                .setParameter("playerAmaxAge", node.hasNonNull("playerAmaxAge") ? parseLocalDate(node.get(
                    "playerAmaxAge")) : null)
                .setParameter("playerBdifferent", node.hasNonNull("playerBdifferent") ? node.get("playerBdifferent")
                    .asBoolean() : null)
                .setParameter("playerBSex", node.hasNonNull("playerBSex") ? SexFilter.valueOf(node.get("playerBSex")
                    .asText()).ordinal() : null)
                .setParameter("playerBhasMinAge", node.hasNonNull("playerBhasMinAge") ? node.get("playerBhasMinAge")
                    .asBoolean() : null)
                .setParameter("playerBminAge", node.hasNonNull("playerBminAge") ? parseLocalDate(node.get(
                    "playerBminAge")) : null)
                .setParameter("playerBhasMaxAge", node.hasNonNull("playerBhasMaxAge") ? node.get("playerBhasMaxAge")
                    .asBoolean() : null)
                .setParameter("playerBmaxAge", node.hasNonNull("playerBmaxAge") ? parseLocalDate(node.get(
                    "playerBmaxAge")) : null)
                .setParameter("cProgress", node.hasNonNull("cProgress") ? CreationProgress.valueOf(node.get("cProgress")
                    .asText()).ordinal() : null)
                .setParameter("totalRounds", node.hasNonNull("totalRounds") ? node.get("totalRounds").asInt() : null)
                .setParameter("tournamentId", getUUID(node, "tournament"))
                .setParameter("finaleId", null)
                .setParameter("thirdPlaceId", null)
                .executeUpdate();
        }
    }

    private void restoreTeams(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO teams (id, competition, player_a, player_b) VALUES (:id, :competition, :playerA, :playerB)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("competition", getUUID(node, "competition"))
                .setParameter("playerA", getUUID(node, "playerA"))
                .setParameter("playerB", getUUID(node, "playerB"))
                .executeUpdate();
        }
    }

    private void restoreMatches(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO matches (id, competition_id, court, begin_time, end_time, finished, winner, team_a, team_b, number) " + "VALUES (:id, :competitionId, :court, :beginTime, :endTime, :finished, :winner, :teamA, :teamB, :number)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("competitionId", getUUID(node, "competition"))
                .setParameter("court", getText(node, "court"))
                .setParameter("beginTime", node.hasNonNull("begin") ? java.sql.Timestamp.from(parseInstant(node.get(
                    "begin"))) : null)
                .setParameter("endTime", node.hasNonNull("end") ? java.sql.Timestamp.from(parseInstant(node.get(
                    "end"))) : null)
                .setParameter("finished", node.hasNonNull("finished") ? node.get("finished").asBoolean() : false)
                .setParameter("winner", node.hasNonNull("winner") ? node.get("winner").asBoolean() : null)
                .setParameter("teamA", getUUID(node, "teamA"))
                .setParameter("teamB", getUUID(node, "teamB"))
                .setParameter("number", node.hasNonNull("number") ? node.get("number").asInt() : null)
                .executeUpdate();
        }
    }

    private void restoreGroups(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO groups (id, index, competition_id) VALUES (:id, :index, :competitionId)")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("index", node.hasNonNull("index") ? (byte) node.get("index").asInt() : null)
                .setParameter("competitionId", getUUID(node, "competition"))
                .executeUpdate();
        }
    }

    private void restoreMatchesOfGroup(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO match_of_group (match, group_id) VALUES (:match, :groupId)")
                .setParameter("match", getUUID(node, "match"))
                .setParameter("groupId", getUUID(node, "group"))
                .executeUpdate();
        }
    }

    private void restoreFinalsOfGroup(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO final_of_group (next_match, \"position\", group_a, group_b) VALUES (:nextMatch, :pos, :groupA, :groupB)")
                .setParameter("nextMatch", getUUID(node, "nextMatch"))
                .setParameter("pos", node.hasNonNull("pos") ? node.get("pos").asInt() : null)
                .setParameter("groupA", getUUID(node, "groupA"))
                .setParameter("groupB", getUUID(node, "groupB"))
                .executeUpdate();
        }
    }

    private void restoreNextMatches(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "INSERT INTO dependant_matches (next_match, previous_a, previous_b, winner) VALUES (:nextMatch, :previousA, :previousB, :winner)")
                .setParameter("nextMatch", getUUID(node, "nextMatch"))
                .setParameter("previousA", getUUID(node, "previousA"))
                .setParameter("previousB", getUUID(node, "previousB"))
                .setParameter("winner", node.hasNonNull("winner") ? node.get("winner").asBoolean() : null)
                .executeUpdate();
        }
    }

    private void restoreSets(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            UUID matchId = null;
            Integer index = null;
            if (node.hasNonNull("key")) {
                JsonNode keyNode = node.get("key");
                if (keyNode.hasNonNull("match")) matchId = UUID.fromString(keyNode.get("match").asText());
                if (keyNode.hasNonNull("index")) index = keyNode.get("index").asInt();
            }
            getEntityManager().createNativeQuery(
                "INSERT INTO sets (match_id, \"index\", score_a, score_b) VALUES (:matchId, :index, :scoreA, :scoreB)")
                .setParameter("matchId", matchId)
                .setParameter("index", index)
                .setParameter("scoreA", node.hasNonNull("scoreA") ? (byte) node.get("scoreA").asInt() : null)
                .setParameter("scoreB", node.hasNonNull("scoreB") ? (byte) node.get("scoreB").asInt() : null)
                .executeUpdate();
        }
    }

    private void finalizeCompetitions(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) return;
        for (JsonNode node : nodes) {
            getEntityManager().createNativeQuery(
                "UPDATE competitions SET finale_id = :finaleId, thirdPlace_id = :thirdPlaceId WHERE id = :id")
                .setParameter("id", getUUID(node, "id"))
                .setParameter("finaleId", getUUID(node, "finale"))
                .setParameter("thirdPlaceId", getUUID(node, "thirdPlace"))
                .executeUpdate();
        }
    }

    private void dumpTable(ZipOutputStream zos, String tableName, PanacheRepository<?> repository) throws IOException {
        zos.putNextEntry(new ZipEntry(tableName + ".json"));
        zos.write(objectMapper.writeValueAsBytes(repository.listAll()));
        zos.closeEntry();
    }

    @POST
    @RolesAllowed("admin")
    @Path("/generateDownload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response generateDownloadLink() throws IOException {
        UUID downloadToken = backupDownloadTokenCache.generateDownloadToken();
        return Response.ok(downloadToken.toString()).build();
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@QueryParam("token") UUID token) throws IOException {
        if (token == null || backupDownloadTokenCache.consumeToken(token).isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing Token").build();
        }

        String filename = "turnierplaner_" + LocalDate.now() + ".turnier";

        StreamingOutput stream = os -> {
            try (ZipOutputStream zos = new ZipOutputStream(os)) {
                dumpTable(zos, "tournaments", tournamentRepository);
                dumpTable(zos, "competitions", competitionRepository);
                dumpTable(zos, "matches", matchRepository);

                dumpTable(zos, "players", playerRepository);
                dumpTable(zos, "teams", teamRepository);

                dumpTable(zos, "courts", courtRepository);

                dumpTable(zos, "groups", groupRepository);
                dumpTable(zos, "matchesOfGroup", matchOfGroupRepository);
                dumpTable(zos, "finalsOfGroup", finalOfGroupRepository);

                dumpTable(zos, "nextMatches", nextMatchRepository);
                dumpTable(zos, "sets", setRepository);

                dumpTable(zos, "config", configRepository);
                dumpTable(zos, "defaultConfig", defaultConfigRepository);

                zos.closeEntry();
                zos.finish();
            }
        };

        return Response.ok(stream)
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .build();
    }


    @POST
    @RolesAllowed("admin")
    @Path("/upload")
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response upload(InputStream inputStream) throws IOException {
        log.info("Starting to restore backup...");
        log.info("Deleting all existing data...");
        configRepository.deleteAll();
        defaultConfigRepository.deleteAll();
        setRepository.deleteAll();
        nextMatchRepository.deleteAll();
        finalOfGroupRepository.deleteAll();
        matchOfGroupRepository.deleteAll();
        matchRepository.deleteAll();
        groupRepository.deleteAll();
        teamRepository.deleteAll();
        playerRepository.deleteAll();
        competitionRepository.deleteAll();
        tournamentRepository.deleteAll();
        courtRepository.deleteAll();
        getEntityManager().flush();
        getEntityManager().clear();
        log.info("All existing data deleted!");

        Map<String, JsonNode> extractedFiles = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                    JsonNode rootNode = objectMapper.reader().without(JsonParser.Feature.AUTO_CLOSE_SOURCE).readTree(
                        zis);
                    extractedFiles.put(entry.getName(), rootNode);
                }
                zis.closeEntry();
            }
        }

        restoreConfig(extractedFiles.get("config.json"));
        restoreDefaultConfig(extractedFiles.get("defaultConfig.json"));
        restoreCourts(extractedFiles.get("courts.json"));
        restorePlayers(extractedFiles.get("players.json"));
        restoreTournaments(extractedFiles.get("tournaments.json"));
        restoreCompetitions(extractedFiles.get("competitions.json"));
        restoreTeams(extractedFiles.get("teams.json"));
        restoreMatches(extractedFiles.get("matches.json"));
        restoreGroups(extractedFiles.get("groups.json"));
        restoreMatchesOfGroup(extractedFiles.get("matchesOfGroup.json"));
        restoreFinalsOfGroup(extractedFiles.get("finalsOfGroup.json"));
        restoreNextMatches(extractedFiles.get("nextMatches.json"));
        restoreSets(extractedFiles.get("sets.json"));
        finalizeCompetitions(extractedFiles.get("competitions.json"));

        log.info("Backup successfully restored.");
        return Response.ok("Backup successfully restored.").build();
    }
}
