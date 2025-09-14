package de.secretj12.turnierplaner.resources;


import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.repositories.CompetitionRepository;
import de.secretj12.turnierplaner.db.repositories.MatchRepository;
import de.secretj12.turnierplaner.db.repositories.PlayerRepository;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.model.user.jUserMatchEvent;
import de.secretj12.turnierplaner.tools.CommonHelpers;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Path("/matches")
public class MatchResource {

    @Inject
    TournamentRepository tournaments;
    @Inject
    CompetitionRepository competitions;
    @Inject
    MatchRepository matches;
    @Inject
    PlayerRepository players;

    @Inject
    CommonHelpers commonHelpers;
    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<jUserMatchEvent> getMatches(
                                            @QueryParam("tour") String tourName,
                                            @QueryParam("comp") String compName,
                                            @QueryParam("player") String playerId,
                                            @QueryParam("from") String from,
                                            @QueryParam("to") String to
    ) {
        Tournament tournament = tournaments.getByName(tourName);
        Competition competition = competitions.getByName(tourName, compName);
        Player player = playerId == null ? null : players.findById(UUID.fromString(playerId));
        Instant fromD = from == null ? null : Instant.parse(from);
        Instant toD = to == null ? null : Instant.parse(to);

        if (player == null && tournament == null && !securityIdentity.hasRole("director"))
            throw new BadRequestException("Need to specify at least a tournament or a player");

        return matches.filterMatches(tournament, competition, player, fromD, toD)
            .stream()
            .filter(commonHelpers::isMatchAccessible)
            .map(jUserMatchEvent::new).toList();
    }
}
