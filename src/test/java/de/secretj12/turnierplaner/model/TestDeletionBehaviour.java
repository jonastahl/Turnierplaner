package de.secretj12.turnierplaner.model;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Set;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.groups.FinalOfGroup;
import de.secretj12.turnierplaner.db.entities.groups.Group;
import de.secretj12.turnierplaner.db.entities.groups.MatchOfGroup;
import de.secretj12.turnierplaner.db.entities.knockout.NextMatch;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.enums.*;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TestDeletionBehaviour {

    @Inject
    TournamentRepository tournamentRepository;
    @Inject
    CompetitionRepository competitionRepository;
    @Inject
    MatchRepository matchRepository;
    @Inject
    NextMatchRepository nextMatchRepository;
    @Inject
    FinalOfGroupRepository finalOfGroupRepository;
    @Inject
    GroupRepository groupRepository;
    @Inject
    MatchOfGroupRepository matchOfGroupRepository;
    @Inject
    SetRepository setRepository;

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
        finalOfGroupRepository.deleteAll();
        nextMatchRepository.deleteAll();
        matchRepository.deleteAll();
        groupRepository.deleteAll();
        competitionRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Test
    @Transactional
    public void nextDeletion() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        Match f = new Match();
        f.setCompetition(competition);
        matchRepository.persist(f);
        Match a = new Match();
        a.setCompetition(competition);
        matchRepository.persist(a);
        Match b = new Match();
        b.setCompetition(competition);
        matchRepository.persist(b);
        competition.setFinale(f);
        competitionRepository.persist(competition);

        NextMatch nm = new NextMatch();
        nm.setNextMatch(f);
        nm.setPreviousA(a);
        nm.setPreviousB(b);
        nextMatchRepository.persist(nm);

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        assertEquals(3, matchRepository.listAll().size());
        competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        matchRepository.delete(competition.getFinale().getDependentOn().getPreviousA());
        competition.setFinale(competition.getFinale().getDependentOn().getPreviousB());
        competitionRepository.persist(competition);

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        assertEquals(1, matchRepository.listAll().size());
    }

    @Test
    @Transactional
    public void groupDeletion() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        Group gA = new Group();
        gA.setCompetition(competition);
        gA.setIndex((byte) 0);
        groupRepository.persist(gA);
        Group gB = new Group();
        gB.setCompetition(competition);
        gB.setIndex((byte) 1);
        groupRepository.persist(gB);
        Group gC = new Group();
        gC.setCompetition(competition);
        gC.setIndex((byte) 2);
        groupRepository.persist(gC);
        Group gD = new Group();
        gD.setCompetition(competition);
        gD.setIndex((byte) 3);
        groupRepository.persist(gD);

        Match gmA = new Match();
        gmA.setCompetition(competition);
        matchRepository.persist(gmA);
        Match gmA2 = new Match();
        gmA2.setCompetition(competition);
        matchRepository.persist(gmA2);
        Match gmB = new Match();
        gmB.setCompetition(competition);
        matchRepository.persist(gmB);
        Match gmC = new Match();
        gmC.setCompetition(competition);
        matchRepository.persist(gmC);
        Match gmD = new Match();
        gmD.setCompetition(competition);
        matchRepository.persist(gmD);

        MatchOfGroup mogA = new MatchOfGroup();
        mogA.setMatch(gmA);
        mogA.setGroup(gA);
        matchOfGroupRepository.persist(mogA);
        MatchOfGroup mogA2 = new MatchOfGroup();
        mogA2.setMatch(gmA2);
        mogA2.setGroup(gA);
        matchOfGroupRepository.persist(mogA2);
        MatchOfGroup mogB = new MatchOfGroup();
        mogB.setMatch(gmB);
        mogB.setGroup(gB);
        matchOfGroupRepository.persist(mogB);
        MatchOfGroup mogC = new MatchOfGroup();
        mogC.setMatch(gmC);
        mogC.setGroup(gC);
        matchOfGroupRepository.persist(mogC);
        MatchOfGroup mogD = new MatchOfGroup();
        mogD.setMatch(gmD);
        mogD.setGroup(gD);
        matchOfGroupRepository.persist(mogD);

        Match f = new Match();
        f.setCompetition(competition);
        matchRepository.persist(f);
        Match a = new Match();
        a.setCompetition(competition);
        matchRepository.persist(a);
        Match b = new Match();
        b.setCompetition(competition);
        matchRepository.persist(b);
        competitionRepository.persist(competition);

        NextMatch nm = new NextMatch();
        nm.setNextMatch(f);
        nm.setPreviousA(a);
        nm.setPreviousB(b);
        nextMatchRepository.persist(nm);

        FinalOfGroup fogA = new FinalOfGroup();
        fogA.setGroupA(gA);
        fogA.setGroupB(gB);
        fogA.setNextMatch(a);
        fogA.setPos(1);
        finalOfGroupRepository.persist(fogA);
        FinalOfGroup fogB = new FinalOfGroup();
        fogB.setGroupA(gC);
        fogB.setGroupB(gD);
        fogB.setNextMatch(b);
        fogB.setPos(1);
        finalOfGroupRepository.persist(fogB);

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        assertEquals(8, matchRepository.listAll().size());
        competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");
        List<Group> groups = competition.getGroups();
        groupRepository.delete(groups.getFirst());

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        assertEquals(4, matchRepository.listAll().size());
    }

    @Test
    @Transactional
    public void matchDeletesSets() {
        Competition competition = competitionRepository.getByName("Clubmeisterschaft", "Herren");

        Match m = new Match();
        m.setCompetition(competition);
        matchRepository.persist(m);

        Set.SetKey setKey = new Set.SetKey();
        setKey.setMatch(m);
        setKey.setIndex((byte) 0);
        Set s = new Set();
        s.setKey(setKey);
        setRepository.persist(s);

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        List<Match> all = matchRepository.findAll().list();
        assertEquals(1, all.size());
        matchRepository.delete(all.getFirst());
        assertEquals(0, setRepository.findAll().count());
    }
}
