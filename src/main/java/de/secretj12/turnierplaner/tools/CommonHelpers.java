package de.secretj12.turnierplaner.tools;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.enums.CreationProgress;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CommonHelpers {
    @Inject
    TournamentRepository tournaments;

    @Inject
    SecurityIdentity securityIdentity;

    public void checkTournamentAccessibility(String tourName) {
        checkTournamentAccessibility(tournaments.getByName(tourName));
    }

    public void checkTournamentAccessibility(Tournament tournament) {
        if (tournament == null) throw new NotFoundException("Tournament could not be found");
        if (!isTournamentAccessible(tournament))
            throw new UnauthorizedException("Cannot access tournament");
    }

    public boolean isTournamentAccessible(Tournament tournament) {
        return securityIdentity.hasRole("director") || tournament.isVisible();
    }

    public boolean isMatchAccessible(Match match) {
        return isTournamentAccessible(match.getCompetition().getTournament()) &&
            (securityIdentity.hasRole("director") || match.getCompetition().getcProgress() == CreationProgress.DONE);
    }
}
