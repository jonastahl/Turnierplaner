package de.secretj12.turnierplaner.resources;


import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Set;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.repositories.MatchRepository;
import de.secretj12.turnierplaner.db.repositories.SetRepository;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.model.user.jUserSet;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RolesAllowed("reporter")
@Path("/tournament/{tourName}/competition/{matchId}/set")
public class SetResource {
    @Inject
    TournamentRepository tournaments;
    @Inject
    SetRepository setRepository;
    @Inject
    MatchRepository matchRepository;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String updateMatches(@PathParam("tourName") String tourName, @PathParam("matchId") UUID matchId,
                                List<jUserSet> sets) {
        Tournament tournament = tournaments.getByName(tourName);
        Instant beginGamePhase = tournament.getBeginGamePhase();
        if (beginGamePhase != null && beginGamePhase.isAfter(Instant.now()))
            throw new UnauthorizedException("Cannot update matches before game phase has begun");

        Match match = matchRepository.findById(matchId);
        if (match == null)
            throw new InternalServerErrorException("Could find match");

        for (jUserSet jSet : sets) {
            Set.SetKey setKey = new Set.SetKey();
            setKey.setMatch(match);
            setKey.setIndex(jSet.getIndex());

            Set set = setRepository.findById(setKey);

            if (set != null && jSet.getScoreA() == 0 && jSet.getScoreB() == 0) {
                setRepository.delete(set);
            } else {
                if (set == null) {
                    set = new Set();
                    set.setKey(setKey);
                }

                set.setScoreA(jSet.getScoreA());
                set.setScoreB(jSet.getScoreB());
                setRepository.persist(set);
            }
        }

        return "Updated matches";
    }


}
