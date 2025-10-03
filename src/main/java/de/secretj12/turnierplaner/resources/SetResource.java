package de.secretj12.turnierplaner.resources;


import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Set;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.groups.Group;
import de.secretj12.turnierplaner.db.entities.knockout.NextMatch;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.enums.NumberSets;
import de.secretj12.turnierplaner.model.user.jUserSet;
import de.secretj12.turnierplaner.model.user.jUserTeamGroupResult;
import de.secretj12.turnierplaner.tools.GroupTools;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Path("/tournament/{tourId}/competition/{compId}/set/{matchId}")
public class SetResource {
    @Inject
    TournamentRepository tournaments;
    @Inject
    CompetitionRepository competitions;
    @Inject
    SetRepository setRepository;
    @Inject
    MatchRepository matchRepository;
    @Inject
    TeamRepository teamRepository;

    @Inject
    GroupTools groupTools;
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Everybody can report the first result of a match after the game has started.
     * Only reporters can update existing results.
     * Directors can set a result before the game has started and remove a result.
     * After every update the players get an update-mail about the result and their upcoming matches.
     * There is no update-mail when a match result is reseted.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String updateMatches(@PathParam("tourId") String tourId, @PathParam("compId") String compId,
                                @PathParam("matchId") UUID matchId,
                                List<jUserSet> sets) {
        Tournament tournament = tournaments.getByName(tourId);
        Competition competition = competitions.getByName(tourId, compId);
        if (tournament == null)
            throw new NotFoundException("Tournament was not found");
        if (competition == null)
            throw new NotFoundException("Competition was not found");

        Instant beginGamePhase = tournament.getBeginGamePhase();
        if (beginGamePhase != null && beginGamePhase.isAfter(Instant.now()) && !securityIdentity.hasRole("director"))
            throw new UnauthorizedException("Cannot update matches before game phase has begun");

        Match match = matchRepository.findById(matchId);
        if (match == null)
            throw new InternalServerErrorException("Could find match");
        boolean firstResult = match.getSets().isEmpty();
        if (!firstResult && !securityIdentity.hasRole("reporter"))
            throw new UnauthorizedException("Cannot update existing match result");

        match.getSets().forEach(setRepository::delete);
        if (securityIdentity.hasRole("director") && sets.isEmpty()) {
            match.setFinished(false);
            adjustNext(match);
            return "Result was reset";
        }

        match.setFinished(true);
        match.setWinner(findWinner(sets, competition.getNumberSets()));

        for (byte i = 0; i < sets.size(); i++) {
            jUserSet jSet = sets.get(i);
            Set.SetKey setKey = new Set.SetKey();
            setKey.setMatch(match);
            setKey.setIndex(i);
            Set set = new Set();
            set.setKey(setKey);
            set.setScoreA(jSet.getScoreA());
            set.setScoreB(jSet.getScoreB());
            setRepository.persist(set);
        }

        matchRepository.persist(match);
        adjustNext(match);
        return "Updated matches";
    }

    private void adjustNext(Match match) {
        if (match.getPreviousOfA() != null) {
            for (NextMatch nm : match.getPreviousOfA()) {
                Match nMatch = nm.getNextMatch();
                nMatch.setTeamA(match.isFinished() ? match.getWinner() ^ nm.isWinner() ? match.getTeamB() : match
                    .getTeamA() : null);
                matchRepository.persist(nMatch);
                adjustNext(nMatch);
            }
        }
        if (match.getPreviousOfB() != null) {
            for (NextMatch nm : match.getPreviousOfB()) {
                Match nMatch = nm.getNextMatch();
                nMatch.setTeamB(match.isFinished() ? match.getWinner() ^ nm.isWinner() ? match.getTeamB() : match
                    .getTeamA() : null);
                matchRepository.persist(nMatch);
                adjustNext(nMatch);
            }
        }
        if (match.getGroup() != null) {
            Group group = match.getGroup().getGroup();
            boolean isFinished = groupTools.isFinished(match.getGroup().getGroup());
            List<jUserTeamGroupResult> results = isFinished ? groupTools.determineGroupResults(group) : List.of();

            for (var fog : group.getFinalOfGroupA()) {
                Match fin = fog.getNextMatch();
                fin.setTeamA(isFinished ? teamRepository.findById(results.get(fog.getPos() - 1).getTeam()
                    .getId()) : null);
                adjustNext(fin);
            }
            for (var fog : group.getFinalOfGroupB()) {
                Match fin = fog.getNextMatch();
                fin.setTeamB(isFinished ? teamRepository.findById(results.get(fog.getPos() - 1).getTeam()
                    .getId()) : null);
                adjustNext(fin);
            }
        }
    }

    private boolean findWinner(List<jUserSet> sets, NumberSets numberSets) {
        int dif = 0;
        Iterator<jUserSet> it = sets.iterator();
        for (byte i = 0; i < 2; i++) {
            notNull(it);
            dif += calcDif(isValidSet(it));
        }

        switch (numberSets) {
            case THREE -> {
                if (Math.abs(dif) == 2)
                    isNull(it);
                else
                    dif += calcDif(isValidTiebreak(it));
            }
            case FIVE -> dif += calcDif(isValidSet(it));
        }
        switch (numberSets) {
            case THREE -> isNull(it);
            case FIVE -> {
                if (Math.abs(dif) == 3)
                    isNull(it);
                else
                    dif += calcDif(isValidSet(it));
            }
        }
        switch (numberSets) {
            case THREE -> isNull(it);
            case FIVE -> {
                if (Math.abs(dif) == 3)
                    isNull(it);
                else
                    dif += calcDif(isValidTiebreak(it));
            }
        }
        if (dif == 0)
            throw new InternalServerErrorException("Dif should 0");
        isNull(it);
        return dif > 0;
    }

    private int calcDif(jUserSet set) {
        if (set.getScoreA() > set.getScoreB())
            return 1;
        else
            return -1;
    }

    private static final java.util.Set<Tuple2<Byte, Byte>> validResults = java.util.Set.of(
        Tuple2.of((byte) 6, (byte) 0),
        Tuple2.of((byte) 6, (byte) 1),
        Tuple2.of((byte) 6, (byte) 2),
        Tuple2.of((byte) 6, (byte) 3),
        Tuple2.of((byte) 6, (byte) 4),
        Tuple2.of((byte) 7, (byte) 5),
        Tuple2.of((byte) 7, (byte) 6),
        Tuple2.of((byte) 0, (byte) 6),
        Tuple2.of((byte) 1, (byte) 6),
        Tuple2.of((byte) 2, (byte) 6),
        Tuple2.of((byte) 3, (byte) 6),
        Tuple2.of((byte) 4, (byte) 6),
        Tuple2.of((byte) 5, (byte) 7),
        Tuple2.of((byte) 6, (byte) 7)
    );

    private jUserSet isValidSet(Iterator<jUserSet> it) {
        notNull(it);
        jUserSet set = it.next();
        byte scoreA = set.getScoreA();
        byte scoreB = set.getScoreB();
        if (!validResults.contains(Tuple2.of(scoreA, scoreB)))
            throw new BadRequestException("Invalid result");
        return set;
    }

    private jUserSet isValidTiebreak(Iterator<jUserSet> it) {
        notNull(it);
        jUserSet set = it.next();
        int scoreA = set.getScoreA();
        int scoreB = set.getScoreB();
        boolean valid = (scoreA == 10 && scoreB <= 8)
            || (scoreB == 10 && scoreA <= 8)
            || (scoreA == scoreB + 2 && scoreB >= 9)
            || (scoreB == scoreA + 2 && scoreA >= 9);
        if (!valid)
            throw new BadRequestException("Invalid tiebreak");
        return set;
    }

    private void notNull(Iterator<jUserSet> it) {
        if (!it.hasNext())
            throw new BadRequestException("Set is not null");
    }

    private void isNull(Iterator<jUserSet> it) {
        if (it.hasNext())
            throw new BadRequestException("Set is null");
    }

}
