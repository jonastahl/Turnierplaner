package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.startup.testdata.TestdataGenerator;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class TestBackup {

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
    TestdataGenerator testdataGenerator;

    @Test
    @TestSecurity(user = "admin", roles = "director")
    public void testBackupCycle() {
        // 1. Generate random data
        generateTestData();

        // 2. Capture initial state (counts)
        long tournamentCount = tournamentRepository.count();
        long competitionCount = competitionRepository.count();
        long matchCount = matchRepository.count();
        long playerCount = playerRepository.count();
        long courtCount = courtRepository.count();
        long groupCount = groupRepository.count();
        long teamCount = teamRepository.count();
        long setCount = setRepository.count();

        // Ensure we actually have data
        assertTrue(tournamentCount > 0, "Should have at least one tournament");
        assertTrue(playerCount > 0, "Should have at least one player");
        assertTrue(courtCount > 0, "Should have at least one court");

        // 3. Download everything
        byte[] backupData = given()
            .when()
            .get("/backup/download")
            .then()
            .statusCode(200)
            .extract().asByteArray();

        // 4. Delete all the data
        deleteAllData();

        // Verify it's empty
        assertEquals(0, tournamentRepository.count(), "Tournaments should be deleted");
        assertEquals(0, playerRepository.count(), "Players should be deleted");
        assertEquals(0, courtRepository.count(), "Courts should be deleted");
        assertEquals(0, matchRepository.count(), "Matches should be deleted (cascade)");
        assertEquals(0, teamRepository.count(), "Teams should be deleted (cascade)");

        // 5. Upload the data
        given()
            .contentType(ContentType.JSON)
            .body(backupData)
            .when()
            .post("/backup/upload")
            .then()
            .statusCode(200);

        // 6. Verify data is the same (check counts)
        assertEquals(tournamentCount, tournamentRepository.count(), "Tournament count should match");
        assertEquals(competitionCount, competitionRepository.count(), "Competition count should match");
        assertEquals(matchCount, matchRepository.count(), "Match count should match");
        assertEquals(playerCount, playerRepository.count(), "Player count should match");
        assertEquals(courtCount, courtRepository.count(), "Court count should match");
        assertEquals(groupCount, groupRepository.count(), "Group count should match");
        assertEquals(teamCount, teamRepository.count(), "Team count should match");
        assertEquals(setCount, setRepository.count(), "Set count should match");
    }

    @Transactional
    public void generateTestData() {
        testdataGenerator.generateData();
        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();
    }

    @Transactional
    public void deleteAllData() {
        tournamentRepository.findAll().stream().forEach(t -> tournamentRepository.delete(t));
        playerRepository.deleteAll();
        courtRepository.deleteAll();
        
        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();
    }
}
