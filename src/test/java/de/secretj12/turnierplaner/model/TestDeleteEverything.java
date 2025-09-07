package de.secretj12.turnierplaner.model;


import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.startup.testdata.TestdataGenerator;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TestDeleteEverything {

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
    @Inject
    TeamRepository teamRepository;

    @Inject
    TestdataGenerator testdataGenerator;

    @Test
    @Transactional
    public void deleteEverything() {
        testdataGenerator.generateData();

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        tournamentRepository.findAll()
            .stream().forEach(t -> tournamentRepository.delete(t));

        Panache.getEntityManager().flush();
        Panache.getEntityManager().clear();

        assertEquals(0, tournamentRepository.count());
        assertEquals(0, competitionRepository.count());
        assertEquals(0, matchRepository.count());
        assertEquals(0, nextMatchRepository.count());
        assertEquals(0, finalOfGroupRepository.count());
        assertEquals(0, groupRepository.count());
        assertEquals(0, matchOfGroupRepository.count());
        assertEquals(0, setRepository.count());
        assertEquals(0, teamRepository.count());
    }
}
