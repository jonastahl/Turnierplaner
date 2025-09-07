package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Court;
import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.repositories.CompetitionRepository;
import de.secretj12.turnierplaner.db.repositories.CourtRepositiory;
import de.secretj12.turnierplaner.db.repositories.MatchRepository;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.enums.*;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.*;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static de.secretj12.turnierplaner.enums.CreationProgress.SCHEDULING;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TestMatchResource {

    @Inject
    MatchRepository matches;
    @Inject
    CourtRepositiory courts;
    @Inject
    CompetitionRepository competitions;
    @Inject
    TournamentRepository tournaments;

    private Match matchE;
    private Match matchF;

    private final Instant beginE = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private final Instant endE = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(90, ChronoUnit.MINUTES);
    private final Instant beginF = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(90, ChronoUnit.MINUTES);
    private final Instant endF = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(180, ChronoUnit.MINUTES);

    @BeforeEach
    @Transactional
    public void addCourts() {
        Tournament tour = new Tournament();
        tour.setName("Clubmeisterschaft");
        tour.setDescription("Anmeldung vorbei");
        tour.setBeginRegistration(Instant.now().minus(12, ChronoUnit.DAYS));
        tour.setEndRegistration(Instant.now().plus(11, ChronoUnit.DAYS));
        tour.setBeginGamePhase(Instant.now().plus(12, ChronoUnit.DAYS));
        tour.setEndGamePhase(Instant.now().plus(13, ChronoUnit.DAYS));
        tour.setVisible(true);
        tournaments.persist(tour);

        Competition comp = new Competition();
        comp.setName("Herren");
        comp.setDescription("Offen für alle Herren");
        comp.setMode(CompetitionMode.SINGLE);
        comp.setType(CompetitionType.KNOCKOUT);
        comp.setPlayerASex(SexFilter.MALE);
        comp.setcProgress(SCHEDULING);
        comp.setSignup(CompetitionSignUp.INDIVIDUAL);
        comp.setPlayerASex(SexFilter.ANY);
        comp.setTournament(tour);
        comp.setNumberSets(NumberSets.THREE);
        competitions.persist(comp);

        Competition comp2 = new Competition();
        comp2.setName("Damen");
        comp2.setDescription("Offen für alle Herren");
        comp2.setMode(CompetitionMode.SINGLE);
        comp2.setType(CompetitionType.KNOCKOUT);
        comp2.setPlayerASex(SexFilter.MALE);
        comp2.setcProgress(SCHEDULING);
        comp2.setSignup(CompetitionSignUp.INDIVIDUAL);
        comp2.setPlayerASex(SexFilter.ANY);
        comp2.setTournament(tour);
        comp2.setNumberSets(NumberSets.THREE);
        competitions.persist(comp2);

        for (int i = 0; i < 5; i++) {
            Court court = new Court();
            court.setCourtType(CourtType.CLAY);
            court.setName("Court " + i);
            courts.persist(court);
        }

        matchE = new Match();
        matchE.setCompetition(comp);
        matches.persist(matchE);

        matchF = new Match();
        matchF.setCompetition(comp);
        matchF.setCourt(courts.findByName("Court 1"));
        matchF.setBegin(Instant.now().plus(15, ChronoUnit.MINUTES));
        matchF.setBegin(Instant.now().plus(120, ChronoUnit.MINUTES));
        matches.persist(matchF);
    }

    @AfterEach
    @Transactional
    public void delete() {
        matches.deleteAll();
        courts.deleteAll();
        competitions.deleteAll();
        tournaments.deleteAll();
    }

    private String updateJSON() {
        return String.format("""
            {
                "complete": false,
                "data": [
                    {
                        "id": "%s",
                        "court": "Court 2",
                        "begin": "%s",
                        "end": "%s"
                    },
                    {
                        "id": "%s",
                        "court": "Court 3",
                        "begin": "%s",
                        "end": "%s"
                    }
                ]
            }""", matchE.getId(), beginE, endE, matchF.getId(), beginF, endF);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"director"})
    public void testUpdateMatch() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        given()
            .contentType(ContentType.JSON)
            .body(updateJSON())
            .post("/tournament/Clubmeisterschaft/competition/Herren/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode());

        Panache.getTransactionManager().begin();
        Match matchE = matches.findById(this.matchE.getId());
        Match matchF = matches.findById(this.matchF.getId());
        assertEquals("Court 2", matchE.getCourt().getName());
        assertEquals("Court 3", matchF.getCourt().getName());

        assertEquals(matchE.getBegin(), beginE);
        assertEquals(matchF.getBegin(), beginF);
        assertEquals(matchE.getEnd(), endE);
        assertEquals(matchF.getEnd(), endF);

        Panache.getTransactionManager().commit();
    }

    @Test
    public void testUnauthorized() {
        given()
            .contentType(ContentType.JSON)
            .body(updateJSON())
            .post("/tournament/Clubmeisterschaft/competition/Herren/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"director"})
    public void unknownTournament() {

        given()
            .contentType(ContentType.JSON)
            .body(updateJSON())
            .post("/tournament/Random turnier/competition/Herren/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"director"})
    public void unknownCompetition() {
        given()
            .contentType(ContentType.JSON)
            .body(updateJSON())
            .post("/tournament/Clubmeisterschaft/competition/Mixed/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"director"})
    public void otherCompetition() {
        given()
            .contentType(ContentType.JSON)
            .body(updateJSON())
            .post("/tournament/Clubmeisterschaft/competition/Damen/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"director"})
    public void unknownMatch() {
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "complete": false,
                  "data": [
                    {
                      "id": "%s",
                      "court": "Court 2",
                      "begin": "%s",
                      "end": "%s"
                    }
                  ]
                }""", UUID.randomUUID(), beginE, endE))
            .post("/tournament/Clubmeisterschaft/competition/Damen/updateSchedule")
            .then().assertThat()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
