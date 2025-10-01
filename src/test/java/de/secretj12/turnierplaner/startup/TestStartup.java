package de.secretj12.turnierplaner.startup;

import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.startup.testdata.TestdataGenerator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TestStartup {

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
    CourtRepositiory courtRepositiory;
    @Inject
    GroupRepository groupRepository;
    @Inject
    NextMatchRepository nextMatchRepository;
    @Inject
    MatchOfGroupRepository matchOfGroupRepository;
    @Inject
    FinalOfGroupRepository finalOfGroupRepository;
    @Inject
    SetRepository setRepository;

    @Inject
    TestdataGenerator testdataGenerator;

    @Test
    public void generateTestdata() {
        testdataGenerator.generateData();
    }

    @AfterEach
    @Transactional
    public void afterEach() {
        setRepository.deleteAll();
        finalOfGroupRepository.deleteAll();
        nextMatchRepository.deleteAll();
        matchOfGroupRepository.deleteAll();
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        groupRepository.deleteAll();
        playerRepository.deleteAll();
        competitionRepository.deleteAll();
        tournamentRepository.deleteAll();
        courtRepositiory.deleteAll();
    }
}
