package de.secretj12.turnierplaner.tools;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.enums.*;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.*;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TestKnockoutTools {

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
        comp.setType(CompetitionType.KNOCKOUT);
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
        nextMatchRepository.deleteAll();
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

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen2Teams() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 2);
        var teams = competition.getTeams();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "complete": false,
                    "data": {
                        "teamA": {
                            "id": "%s"
                        },
                        "teamB": {
                            "id": "%s"
                        }
                    }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(200);

        competitionRepository.getEntityManager().clear();
        competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(1, generated.size());
        assertEquals(teams.get(0).getId(), generated.getFirst().getTeamA().getId());
        assertEquals(teams.get(1).getId(), generated.getFirst().getTeamB().getId());

        assertEquals(generated.getFirst().getId(), competition.getFinale().getId());
        assertNull(competition.getThirdPlace());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen3Teams() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 3);
        var teams = competition.getTeams();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "complete": false,
                  "data": {
                    "previousA": {
                      "teamA": {
                        "id": "%s"
                      }
                    },
                    "previousB": {
                      "teamA": {
                        "id": "%s"
                      },
                      "teamB": {
                        "id": "%s"
                      }
                    }
                  }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(200);

        competitionRepository.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(3, generated.size());

        assertEquals(teams.get(0).getId(), newComp.getFinale().getTeamA().getId());
        assertEquals(teams.get(0).getId(), newComp.getFinale().getDependentOn().getPreviousA().getTeamA().getId());

        assertEquals(teams.get(1).getId(), newComp.getFinale().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(2).getId(), newComp.getFinale().getDependentOn().getPreviousB().getTeamB().getId());

        assertNull(newComp.getThirdPlace());
        assertTrue(generated.stream().map(Match::getId).anyMatch(id -> newComp.getFinale().getId() == id));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen4Teams() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 4);
        var teams = competition.getTeams();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "complete": false,
                  "data": {
                    "previousA": {
                      "teamA": {
                        "id": "%s"
                      },
                      "teamB": {
                        "id": "%s"
                      }
                    },
                    "previousB": {
                      "teamA": {
                        "id": "%s"
                      },
                      "teamB": {
                        "id": "%s"
                      }
                    }
                  }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(200);

        competitionRepository.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(4, generated.size());

        assertEquals(teams.get(0).getId(), newComp.getFinale().getDependentOn().getPreviousA().getTeamA().getId());
        assertEquals(teams.get(1).getId(), newComp.getFinale().getDependentOn().getPreviousA().getTeamB().getId());
        assertEquals(teams.get(2).getId(), newComp.getFinale().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(3).getId(), newComp.getFinale().getDependentOn().getPreviousB().getTeamB().getId());

        assertTrue(generated.stream().map(Match::getId).anyMatch(id -> newComp.getFinale().getId() == id));
        assertNotNull(newComp.getThirdPlace());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousA().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousA().getId());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousB().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen8Teams() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 8);
        var teams = competition.getTeams();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "complete": false,
                  "data": {
                    "previousA": {
                      "previousA": {
                        "teamA": {
                          "id": "%s"
                        },
                        "teamB": {
                          "id": "%s"
                        }
                      },
                      "previousB": {
                        "teamA": {
                          "id": "%s"
                        },
                        "teamB": {
                          "id": "%s"
                        }
                      }
                    },
                    "previousB": {
                      "previousA": {
                        "teamA": {
                          "id": "%s"
                        },
                        "teamB": {
                          "id": "%s"
                        }
                      },
                      "previousB": {
                        "teamA": {
                          "id": "%s"
                        },
                        "teamB": {
                          "id": "%s"
                        }
                      }
                    }
                  }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(200);

        competitionRepository.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(8, generated.size());

        assertEquals(teams.get(0).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getTeamA().getId());
        assertEquals(teams.get(1).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getTeamB().getId());
        assertEquals(teams.get(2).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(3).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getTeamB().getId());
        assertEquals(teams.get(4).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getTeamA().getId());
        assertEquals(teams.get(5).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getTeamB().getId());
        assertEquals(teams.get(6).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(7).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getTeamB().getId());

        assertTrue(generated.stream().map(Match::getId).anyMatch(id -> newComp.getFinale().getId() == id));
        assertNotNull(newComp.getThirdPlace());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousA().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousA().getId());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousB().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen13Teams() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        generateTeams(competition, 12);
        var teams = competition.getTeams();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "complete": false,
                  "data": {
                    "previousA": {
                      "previousA": {
                        "previousA": {
                          "teamA": {
                            "id": "%s"
                          },
                          "teamB": {
                            "id": "%s"
                          }
                        },
                        "previousB": {
                          "teamA": {
                            "id": "%s"
                          }
                        }
                      },
                      "previousB": {
                        "previousA": {
                          "teamB": {
                            "id": "%s"
                          }
                        },
                        "previousB": {
                          "teamB": {
                            "id": "%s"
                          }
                        }
                      }
                    },
                    "previousB": {
                      "previousA": {
                        "previousA": {
                          "teamA": {
                            "id": "%s"
                          },
                          "teamB": {
                            "id": "%s"
                          }
                        },
                        "previousB": {
                          "teamA": {
                            "id": "%s"
                          },
                          "teamB": {
                            "id": "%s"
                          }
                        }
                      },
                      "previousB": {
                        "previousA": {
                          "teamA": {
                            "id": "%s"
                          }
                        },
                        "previousB": {
                          "teamA": {
                            "id": "%s"
                          },
                          "teamB": {
                            "id": "%s"
                          }
                        }
                      }
                    }
                  }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(200);

        competitionRepository.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(16, generated.size());

        assertEquals(teams.get(0).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousA()
                .getTeamA().getId());
        assertEquals(teams.get(1).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousA()
                .getTeamB().getId());
        assertEquals(teams.get(2).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousB()
                .getTeamA().getId());
        assertEquals(teams.get(2).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA().getTeamB().getId());
        assertEquals(teams.get(3).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getDependentOn()
                .getPreviousA()
                .getTeamB().getId());
        assertEquals(teams.get(3).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(4).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getDependentOn()
                .getPreviousB()
                .getTeamB().getId());
        assertEquals(teams.get(4).getId(),
            newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB().getTeamB().getId());
        assertEquals(teams.get(5).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousA()
                .getTeamA().getId());
        assertEquals(teams.get(6).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousA()
                .getTeamB().getId());
        assertEquals(teams.get(7).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousB()
                .getTeamA().getId());
        assertEquals(teams.get(8).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA().getDependentOn()
                .getPreviousB()
                .getTeamB().getId());
        assertEquals(teams.get(9).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getDependentOn()
                .getPreviousA()
                .getTeamA().getId());
        assertEquals(teams.get(9).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getTeamA().getId());
        assertEquals(teams.get(10).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getDependentOn()
                .getPreviousB()
                .getTeamA().getId());
        assertEquals(teams.get(11).getId(),
            newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB().getDependentOn()
                .getPreviousB()
                .getTeamB().getId());

        assertTrue(generated.stream().map(Match::getId).anyMatch(id -> newComp.getFinale().getId() == id));
        assertNotNull(newComp.getThirdPlace());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousA().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousA().getId());
        assertEquals(newComp.getFinale().getDependentOn().getPreviousB().getId(),
            newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
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
                    "teamA": {
                      "id": "%s"
                    },
                    "teamB": {
                      "id": "%s"
                    }
                  }
                }
                """, teams.stream().map(Team::getId).toArray()))
            .post("/tournament/Clubmeisterschaft/competition/Herren/initKnockout")
            .then()
            .assertThat()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen2to3Teams() {
        gen2Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen3Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen3to2Teams() {
        gen3Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen2Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen2to4Teams() {
        gen2Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen4Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen4to2Teams() {
        gen2Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen2Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen3to4Teams() {
        gen3Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match a = competition.getFinale().getDependentOn().getPreviousA();

        Panache.getEntityManager().clear();
        gen4Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen4to3Teams() {
        gen4Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match a = competition.getFinale().getDependentOn().getPreviousA();

        Panache.getEntityManager().clear();
        gen3Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen2to8Teams() {
        gen2Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen8Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen3to8Teams() {
        gen3Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match a = competition.getFinale().getDependentOn().getPreviousA();

        Panache.getEntityManager().clear();
        gen8Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen4to8Teams() {
        gen4Teams();
        competitionRepository.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match thi = competition.getThirdPlace();
        Match a = fin.getDependentOn().getPreviousA();
        Match b = fin.getDependentOn().getPreviousB();
        competitionRepository.getEntityManager().clear();

        gen8Teams();

        competitionRepository.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Match> generated = matchRepository.listAll();
        assertEquals(8, generated.size());

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(thi.getId(), newComp.getThirdPlace().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
        assertEquals(b.getId(), newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen8to2Teams() {
        gen8Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen2Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen8to3Teams() {
        gen8Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match a = competition.getFinale().getDependentOn().getPreviousA();

        Panache.getEntityManager().clear();
        gen3Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen8to4Teams() {
        gen8Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match thi = competition.getThirdPlace();
        Match a = fin.getDependentOn().getPreviousA();
        Match b = fin.getDependentOn().getPreviousB();

        Panache.getEntityManager().clear();
        gen4Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(thi.getId(), newComp.getThirdPlace().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
        assertEquals(b.getId(), newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen2to2Teams() {
        gen2Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();

        Panache.getEntityManager().clear();
        gen2Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen3to3Teams() {
        gen3Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match a = competition.getFinale().getDependentOn().getPreviousA();

        Panache.getEntityManager().clear();
        gen3Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen4to4Teams() {
        gen4Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match thi = competition.getThirdPlace();
        Match a = fin.getDependentOn().getPreviousA();
        Match b = fin.getDependentOn().getPreviousB();

        Panache.getEntityManager().clear();
        gen4Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(thi.getId(), newComp.getThirdPlace().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
        assertEquals(b.getId(), newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "director")
    public void gen8to8Teams() {
        gen8Teams();
        Panache.getEntityManager().clear();
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        Match fin = competition.getFinale();
        Match thi = competition.getThirdPlace();
        Match a = fin.getDependentOn().getPreviousA();
        Match b = fin.getDependentOn().getPreviousB();
        Match aa = a.getDependentOn().getPreviousA();
        Match ab = a.getDependentOn().getPreviousB();
        Match ba = b.getDependentOn().getPreviousA();
        Match bb = b.getDependentOn().getPreviousB();

        Panache.getEntityManager().clear();
        gen8Teams();

        Panache.getEntityManager().clear();
        Competition newComp = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        assertEquals(fin.getId(), newComp.getFinale().getId());
        assertEquals(thi.getId(), newComp.getThirdPlace().getId());
        assertEquals(a.getId(), newComp.getFinale().getDependentOn().getPreviousA().getId());
        assertEquals(b.getId(), newComp.getThirdPlace().getDependentOn().getPreviousB().getId());
        assertEquals(aa.getId(), newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousA()
            .getId());
        assertEquals(ab.getId(), newComp.getFinale().getDependentOn().getPreviousA().getDependentOn().getPreviousB()
            .getId());
        assertEquals(ba.getId(), newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousA()
            .getId());
        assertEquals(bb.getId(), newComp.getFinale().getDependentOn().getPreviousB().getDependentOn().getPreviousB()
            .getId());
    }
}
