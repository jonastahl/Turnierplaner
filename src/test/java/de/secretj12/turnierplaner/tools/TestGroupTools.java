package de.secretj12.turnierplaner.tools;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.enums.*;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.*;
import de.secretj12.turnierplaner.db.entities.groups.Group;
import de.secretj12.turnierplaner.db.repositories.*;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TestGroupTools {

    private static final Logger log = LoggerFactory.getLogger(TestGroupTools.class);
    @Inject
    TournamentRepository tournamentRepository;
    @Inject
    CompetitionRepository competitionRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    TeamRepository teamRepository;
    @Inject
    MatchRepository matchRepository;
    @Inject
    NextMatchRepository nextMatchRepository;
    @Inject
    MatchOfGroupRepository matchOfGroupRepository;
    @Inject
    GroupRepository groupRepository;
    @Inject
    FinalOfGroupRepository finalOfGroupRepository;


    @BeforeEach
    @Transactional
    public void addData() {
        Tournament tour = new Tournament();
        tour.setName("Clubmeisterschaft");
        tour.setDescription("Anmeldung abgeschlossen");
        tour.setBeginRegistration(Instant.now().minus(10, ChronoUnit.DAYS));
        tour.setEndRegistration(Instant.now().minus(5, ChronoUnit.DAYS));
        tour.setBeginGamePhase(Instant.now().plus(5, ChronoUnit.DAYS));
        tour.setEndGamePhase(Instant.now().plus(10, ChronoUnit.DAYS));
        tour.setVisible(true);
        tournamentRepository.persist(tour);

        Competition comp = new Competition();
        comp.setName("Herren");
        comp.setDescription("Empty description");
        comp.setMode(CompetitionMode.DOUBLE);
        comp.setType(CompetitionType.GROUPS);
        comp.setSignup(CompetitionSignUp.TOGETHER);
        comp.setPlayerASex(SexFilter.MALE);
        comp.setTournament(tour);
        comp.setcProgress(CreationProgress.GAMES);
        comp.setNumberSets(NumberSets.THREE);
        competitionRepository.persist(comp);
    }

    @AfterEach
    @Transactional
    public void deleteData() {
        matchOfGroupRepository.deleteAll();
        nextMatchRepository.deleteAll();
        finalOfGroupRepository.deleteAll();
        groupRepository.deleteAll();
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        playerRepository.deleteAll();
        competitionRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Transactional
    public void generateTeams(Competition comp, int num) {
        for (int i = 0; i < num; i++) {
            Player pA = new Player();
            pA.setFirstName("firstA");
            pA.setLastName("lastA");
            playerRepository.persist(pA);
            Player pB = new Player();
            pB.setFirstName("firstB");
            pB.setLastName("lastB");
            playerRepository.persist(pB);

            Team team = new Team();
            team.setPlayerA(pA);
            team.setPlayerB(pB);
            team.setCompetition(comp);
            teamRepository.persist(team);
        }
    }

    @ParameterizedTest
    @CsvSource({
                "2, 1",
                "5, 1",
                "10, 1",
                "20, 1",
                "2, 2",
                "5, 2",
                "10, 2",
                "20, 2",
                "2, 4",
                "5, 4",
                "10, 4",
                "20, 4",
                "2, 8",
                "5, 8",
                "10, 8",
                "20, 8",
    })
    @TestSecurity(user = "testuser", roles = "director")
    public void genGroups(int groupSize, int groupCount) {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, groupSize * groupCount);
        var teams = competition.getTeams();

        String teamTemp = "{ \"id\": \"%s\" }";
        String groupTemp = "[ %s ]";
        String bodyTemp = """
            {
                "complete": false,
                "data": {
                    "groups": [
                        %s
                    ]
                }
            }
            """;
        given()
            .contentType(ContentType.JSON)
            .body(String.format(bodyTemp, Stream.iterate(0, n -> n + 1)
                .limit(groupCount)
                .map(gi -> Stream.iterate(0, n -> n + 1).limit(groupSize)
                    .map(ti -> String.format(teamTemp, teams.get(groupSize * gi + ti).getId()))
                    .collect(Collectors.joining(", "))
                )
                .map(s -> String.format(groupTemp, s))
                .collect(Collectors.joining(",\n"))))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initGroups")
            .then()
            .assertThat()
            .statusCode(200);

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Group> groups = newComp.getGroups();

        assertEquals(groupCount, groups.size());
        int ggC = groupSize * (groupSize - 1) / 2;
        for (int i = 0; i < groupCount; i++) {
            Group group = groups.get(i);
            assertEquals(ggC, group.getMatches().size());
            checkGroup(teams.subList(i * groupSize, (i + 1) * groupSize), group.getMatches());
        }

        List<Match> matches = newComp.getMatches();
        int kgC = groupCount == 1 ? 0 : groupCount;
        assertEquals(groupCount * ggC + kgC, matches.size());

        Match fin = newComp.getFinale();
        Match thi = newComp.getThirdPlace();
        if (groupCount == 1) {
            assertNull(fin);
            assertNull(thi);
            return;
        } else {
            assertNotNull(fin);
            assertNotNull(thi);
        }

        if (groupCount == 2) {
            assertEquals(2, thi.getFinalOfGroup().getPos());
            assertEquals(groups.get(0).getId(), thi.getFinalOfGroup().getGroupA().getId());
            assertEquals(groups.get(1).getId(), thi.getFinalOfGroup().getGroupB().getId());
        } else {
            assertFalse(thi.getDependentOn().isWinner());
            assertEquals(fin.getDependentOn().getPreviousA().getId(), thi.getDependentOn().getPreviousA().getId());
            assertEquals(fin.getDependentOn().getPreviousB().getId(), thi.getDependentOn().getPreviousB().getId());
        }
        List<Match> finMatches = flatMatches(List.of(newComp.getFinale()));
        assertEquals(groupCount / 2, finMatches.size());
        for (int i = 0; i < groupCount / 2; i++) {
            assertEquals(groups.get(2 * i).getId(), finMatches.get(i).getFinalOfGroup().getGroupA().getId());
            assertEquals(groups.get(2 * i + 1).getId(), finMatches.get(i).getFinalOfGroup().getGroupB().getId());
        }
    }

    @Test
    public void unauthorized() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 2);
        var teams = competition.getTeams();
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "complete": false,
                    "data": {
                        "groups": [
                            [
                                { "id": "%s" }, { "id": "%s" }
                            ]
                        ]
                    }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initGroups")
            .then()
            .assertThat()
            .statusCode(401);
    }

    @ParameterizedTest
    @CsvSource({
                "4, 1, 4, 1",
                "8, 1, 4, 2",
                "3, 1, 5, 4",
                "3, 1, 7, 8",
                "6, 2, 2, 1",
                "6, 2, 8, 2",
                "5, 2, 2, 4",
                "7, 2, 9, 8",
                "6, 4, 5, 1",
                "6, 4, 2, 2",
                "3, 4, 8, 4",
                "7, 4, 3, 8",
                "5, 8, 8, 1",
                "4, 8, 5, 2",
                "3, 8, 4, 4",
                "2, 8, 3, 8",
                "32, 1, 2, 16",
                "2, 16, 32, 1"
    })
    @TestSecurity(user = "testuser", roles = "director")
    public void genGroupsChange(int groupSizeFrom, int groupCountFrom, int groupSizeTo, int groupCountTo) {
        genGroups(groupSizeFrom, groupCountFrom);

        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Group> groups = competition.getGroups();
        List<Set<Match>> matches = groups.stream().map(Group::getMatches).toList();
        // necessary to preload matches
        matches.forEach(ms -> ms.forEach(Match::getTeamA));
        matches.forEach(ms -> ms.forEach(Match::getTeamB));
        Match fin = competition.getFinale();
        List<Match> firstMatches = groupCountFrom > 1 ? flatMatches(List.of(fin)) : List.of();

        Panache.getEntityManager().clear();
        genGroups(groupSizeTo, groupCountTo);

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Group> newGroups = newComp.getGroups();
        Match newFin = newComp.getFinale();
        List<Match> newFirstMatches = groupCountTo > 1 ? flatMatches(List.of(newFin)) : List.of();

        for (int i = 0; i < Math.min(groupCountFrom, groupCountTo); i++) {
            for (Match m : matches.get(i)) {
                for (Match nm : newGroups.get(i).getMatches()) {
                    if (checkTeams(m.getTeamA(), m.getTeamB(), nm))
                        assertEquals(m.getId(), nm.getId());
                }
            }
        }
        for (int i = 0; i < Math.min(groupCountFrom, groupCountTo); i++) {
            assertEquals(groups.get(i).getId(), newGroups.get(i).getId());
        }
        for (int i = 0; i < Math.min(groupCountFrom, groupCountTo) / 2; i++) {
            assertEquals(firstMatches.get(i).getId(), newFirstMatches.get(i).getId());
        }
    }

    private void checkGroup(List<Team> teams, Set<Match> matches) {
        for (Team t1 : teams) {
            for (Team t2 : teams) {
                if (t1.getId().equals(t2.getId()))
                    continue;

                assertTrue(matches.stream().anyMatch(m -> checkTeams(t1, t2, m)));
            }
        }
    }

    private boolean checkTeams(Team tA, Team tB, Match m) {
        return (m.getTeamA().getId().equals(tA.getId()) && m.getTeamB().getId().equals(tB.getId()))
            || (m.getTeamA().getId().equals(tB.getId()) && m.getTeamB().getId().equals(tA.getId()));
    }

    private List<Match> flatMatches(List<Match> matches) {
        if (matches.getFirst().getDependentOn() == null)
            return matches;
        return flatMatches(matches.stream()
            .flatMap(m -> Stream.of(m.getDependentOn().getPreviousA(), m.getDependentOn().getPreviousB()))
            .toList());
    }
}
