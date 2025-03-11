package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.VerificationCode;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.repositories.CompetitionRepository;
import de.secretj12.turnierplaner.db.repositories.PlayerRepository;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.db.repositories.VerificationCodeRepository;
import de.secretj12.turnierplaner.enums.*;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.*;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class TestPlayerResource {

    @Inject
    TournamentRepository tournamentRepository;
    @Inject
    CompetitionRepository competitionRepository;
    @Inject
    PlayerRepository players;
    @Inject
    VerificationCodeRepository verificationCodes;
    @Inject
    MockMailbox mailbox;

    private Competition genComp() {
        Competition competition = new Competition();
        competition.setcProgress(CreationProgress.TEAMS);
        competition.setMode(CompetitionMode.SINGLE);
        competition.setType(CompetitionType.KNOCKOUT);
        competition.setSignup(CompetitionSignUp.INDIVIDUAL);
        competition.setPlayerASex(SexFilter.ANY);
        competition.setNumberSets(NumberSets.THREE);
        return competition;
    }

    @BeforeEach
    @Transactional
    public void addPlayer() {
        Tournament tournament = new Tournament();
        tournament.setName("Clubmeisterschaft");
        tournament.setVisible(true);
        tournamentRepository.persist(tournament);

        Competition herren = genComp();
        herren.setTournament(tournament);
        herren.setName("Herren");
        herren.setPlayerASex(SexFilter.MALE);
        competitionRepository.persist(herren);

        Competition damen = genComp();
        damen.setTournament(tournament);
        damen.setName("Damen");
        damen.setPlayerASex(SexFilter.FEMALE);
        competitionRepository.persist(damen);

        Competition uX = genComp();
        uX.setTournament(tournament);
        uX.setName("uX");
        uX.setcProgress(CreationProgress.TEAMS);
        uX.setPlayerAhasMaxAge(true);
        uX.setPlayerAmaxAge(LocalDate.parse("2023-04-03"));
        competitionRepository.persist(uX);

        Competition oX = genComp();
        oX.setTournament(tournament);
        oX.setName("oX");
        oX.setPlayerAhasMinAge(true);
        oX.setPlayerAminAge(LocalDate.parse("2023-04-03"));
        competitionRepository.persist(oX);

        for (int i = 0; i < 5; i++) {
            players.persist(
                new Player("M" + i, "F" + i + "V", Sex.MALE, LocalDate.parse(
                    "2023-04-02"), "a@example.org", "+12345", true, true));
        }
        for (int i = 5; i < 10; i++) {
            players.persist(new Player("M" + i, "F" + i, Sex.MALE, LocalDate.parse(
                "2023-04-05"), "a@example.org", "+12345", false, true));
        }
        for (int i = 10; i < 20; i++) {
            players.persist(new Player("M" + i, "F" + i + "V", Sex.MALE, LocalDate.parse(
                "2023-04-05"), "a@example.org", "+12345", true, true));
        }
        for (int i = 0; i < 3; i++) {
            players.persist(new Player("F" + i, "M" + i + "V", Sex.FEMALE, LocalDate.parse(
                "2023-04-02"), "a@example.org", "+12345", true, true));
        }

        players.persist(new Player("first", "last", Sex.MALE, LocalDate.parse(
            "2023-04-02"), "a@example.org", "+12345", true, true));

        mailbox.clear();
    }

    @AfterEach
    @Transactional
    public void delete() {
        verificationCodes.deleteAll();
        players.deleteAll();
        competitionRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Test
    public void listPlayerEmpty() {
        given()
            .param("search", "")
            .get("/player/compFind/Clubmeisterschaft/Herren")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("size()", equalTo(0));
    }

    @Test
    public void listPlayerMale() {
        given()
            .param("search", "M").param("sex", "MALE")
            .get("/player/compFind/Clubmeisterschaft/Herren")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("findAll { it.firstName.startsWith('M') }.size()", equalTo(10));
    }

    @Test
    public void listPlayerFemale() {
        given()
            .param("search", "F").param("sex", "FEMALE")
            .get("/player/compFind/Clubmeisterschaft/Damen")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("findAll { it.firstName.startsWith('F') }.size()", equalTo(3));
    }

    @Test
    public void listPlayerCheckVerified() {
        given()
            .param("search", "M")
            .get("/player/compFind/Clubmeisterschaft/Herren")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode()).and()
            .body("findAll { it.lastName.endsWith('V') }.size()", equalTo(10));
    }

    @Test
    public void listPlayerMaxAge() {
        given()
            .param("search", "M")
            .get("/player/compFind/Clubmeisterschaft/uX")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("size()", equalTo(10));
    }

    @Test
    public void listPlayerMinAge() {
        given()
            .param("search", "M")
            .get("/player/compFind/Clubmeisterschaft/oX")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("size()", equalTo(8));
    }

    @Test
    public void registerPlayerConflict() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "firstName": "first",
                    "lastName": "last",
                    "sex": "MALE",
                    "birthday": "2022-03-10",
                    "email": "ab@example.org",
                    "phone": "+034759834"
                }""")
            .post("/player/registration")
            .then().assertThat()
            .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void registerPlayerStandardFlow() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        String recipient = "ab@example.org";
        String tel = "+034759834";
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "sex": "MALE",
                    "birthday": "1977-03-10",
                    "email": "%s",
                    "phone": "%s"
                }""", recipient, tel))
            .post("/player/registration")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode());

        Panache.getTransactionManager().begin();
        assertNotNull(players.getByName("firstName", "lastName"));
        assertFalse(players.getByName("firstName", "lastName").isMailVerified());
        // TODO admin verified?
        assertEquals(tel, players.getByName("firstName", "lastName").getPhone());

        assertEquals(1, mailbox.getTotalMessagesSent());
        assertDoesNotThrow(() -> mailbox.getMailMessagesSentTo(recipient).getFirst().getHtml());
        String text = mailbox.getMailMessagesSentTo("ab@example.org").getFirst().getHtml();
        String code = text.split("code=")[1].split("\"")[0];
        VerificationCode verificationCode = verificationCodes.findByUUID(UUID.fromString(code));
        Player player = players.getByName("firstName", "lastName");
        assertEquals(verificationCode.getPlayer().getId(), player.getId());
        Panache.getTransactionManager().commit();

        given()
            .param("code", code).get("/player/verification")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode());
        assertTrue(players.getByName("firstName", "lastName").isMailVerified());
        // TODO admin verified?
        assertEquals(tel, players.getByName("firstName", "lastName").getPhone());
    }

    @Test
    public void playerRegistrationFemale() {
        String recipient = "ab@example.org";
        String tel = "+034759834";
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "sex": "FEMALE",
                    "birthday": "1977-03-10",
                    "email": "%s",
                    "phone": "%s"
                }""", recipient, tel))
            .post("/player/registration")
            .then().assertThat()
            .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void playerRegistrationNoSex() {
        String recipient = "ab@example.org";
        String tel = "+034759834";
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "firstName": "firstName",
                    "lastName": "lastName",
                    "birthday": "1977-03-10",
                    "email": "%s",
                    "phone": "%s"
                }""", recipient, tel))
            .post("/player/registration")
            .then().assertThat()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
