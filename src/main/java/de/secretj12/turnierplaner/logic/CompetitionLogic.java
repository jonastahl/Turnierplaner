package de.secretj12.turnierplaner.logic;

import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.competition.Team;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.enums.*;
import de.secretj12.turnierplaner.model.director.competition.jDirectorCompetitionAdd;
import de.secretj12.turnierplaner.model.user.jUserPlayerSignUpForm;
import de.secretj12.turnierplaner.resources.MailTemplates;
import de.secretj12.turnierplaner.resources.PlayerResource;
import de.secretj12.turnierplaner.tools.CommonHelpers;
import de.secretj12.turnierplaner.tools.GroupTools;
import de.secretj12.turnierplaner.tools.KnockoutTools;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CompetitionLogic {
    @Inject
    CommonHelpers common;
    @Inject
    PlayerResource playersResource;
    @Inject
    PlayerRepository players;
    @Inject
    CompetitionRepository competitions;
    @Inject
    TournamentRepository tournaments;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    MatchRepository matches;
    @Inject
    TeamRepository teams;
    @Inject
    CourtRepositiory courts;

    @Inject
    KnockoutTools knockoutTools;
    @Inject
    GroupTools groupTools;
    @Inject
    MailTemplates mailTemplates;

    @Transactional
    public void addCompetitionLogic(String tourName, jDirectorCompetitionAdd competition) {
        if (competition.getName() == null) throw new BadRequestException("No competition specified");

        if (competitions.getByName(tourName, competition.getName()) != null)
            throw new BadRequestException("Competition already exists");

        Tournament tournament = tournaments.getByName(tourName);
        if (tournament == null) throw new BadRequestException("Tournament doesn't exist");

        if (competition.getPlayerA().isHasMinAge() && competition.getPlayerA().getMinAge() == null)
            throw new BadRequestException("Player A: Min age null although has min age");
        if (competition.getPlayerA().isHasMaxAge() && competition.getPlayerA().getMaxAge() == null)
            throw new BadRequestException("Player A: Max age null although has max age");
        if (competition.getPlayerB().isHasMinAge() && competition.getPlayerB().getMinAge() == null)
            throw new BadRequestException("Player B: Min age null although has min age");
        if (competition.getPlayerB().isHasMaxAge() && competition.getPlayerB().getMaxAge() == null)
            throw new BadRequestException("Player B: Max age null although has max age");

        Competition dbCompetition = new Competition();
        competition.toDB(dbCompetition);
        dbCompetition.setcProgress(CreationProgress.TEAMS);
        dbCompetition.setTournament(tournament);
        competitions.persist(dbCompetition);
    }

    @Transactional
    public String signUpPlayerLogic(@PathParam("tourName") String tourName, @PathParam("compName") String compName,
                                    jUserPlayerSignUpForm reg) {
        // TODO better checks if team or members of it are already registered in team
        // TODO check if both layers are the same
        // TODO check if players match conditions
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new BadRequestException("Competition doesn't exist");
        checkRoleAndAccessibility(competition);

        Team team = new Team();
        if (competition.getMode() == CompetitionMode.SINGLE
                || (competition.getSignup() == CompetitionSignUp.INDIVIDUAL && !competition.isPlayerBdifferent())) {
            Player playerA = checkSingleSignUp(reg, competition);
            team.setPlayerA(playerA);
            teams.persist(team);
        } // double
        else if (competition.getSignup() == CompetitionSignUp.INDIVIDUAL && competition.isPlayerBdifferent()) {
            // double mode with individual registration and different constraints
            // -> each registration needs to be player A xor player B null
            Player player = checkIndividualSignUp(reg, competition);
            if (reg.getPlayerA() != null) {
                team.setPlayerA(player);
            } else {
                team.setPlayerB(player);
            }
            teams.persist(team);
        } else {
            // double mode with registration together and any constraints
            // -> needs player A and player B to be not null
            if (reg.getPlayerA() == null) throw new BadRequestException("Player A is null");
            if (reg.getPlayerB() == null) throw new BadRequestException("Player B is null");
            Player playerA = getPlayer(reg.getPlayerA().getId(), true, competition);
            Player playerB = getPlayer(reg.getPlayerB().getId(), false, competition);
            team.setPlayerA(playerA);
            team.setPlayerB(playerB);
        }
        team.setCompetition(competition);
        teams.persist(team);

        if (team.getPlayerA() != null) {
            mailTemplates.sendRegistrationMail(team.getPlayerA(), competition);
        }
        if (team.getPlayerB() != null) {
            mailTemplates.sendRegistrationMail(team.getPlayerB(), competition);
        }

        return "Player registered";
    }


    private Player checkSingleSignUp(jUserPlayerSignUpForm reg, Competition competition) {
        if (reg.getPlayerA() == null) throw new BadRequestException("Player A is null");
        if (reg.getPlayerB() != null) throw new BadRequestException("Player B is not null");

        return getPlayer(reg.getPlayerA().getId(), true, competition);
    }

    private Player checkIndividualSignUp(jUserPlayerSignUpForm reg, Competition competition) {
        if (reg.getPlayerA() == null && reg.getPlayerB() == null || reg.getPlayerA() != null && reg.getPlayerB() != null)
            throw new BadRequestException("You need to register either player A or player B");

        if (reg.getPlayerA() != null) {
            return getPlayer(reg.getPlayerA().getId(), true, competition);
        } else {
            return getPlayer(reg.getPlayerB().getId(), false, competition);
        }
    }


    private Player getPlayer(UUID playerId, boolean playerA, Competition competition) {
        Player player = players.findById(playerId);
        if (player == null)
            if (playerA) throw new BadRequestException("Player A does not exist");
            else throw new BadRequestException("Player does not exist");

        if (playerA && conditionsFailA(competition, player))
            throw new BadRequestException("Player A does not meet the conditions");
        else if (!playerA && conditionsFailB(competition, player))
            throw new BadRequestException("Player B does not meet the conditions");

        checkPlayerAlreadyRegistered(competition.getTeams(), player);
        return player;
    }

    private void checkPlayerAlreadyRegistered(List<Team> regTeams, Player player) {
        if (regTeams != null
                && regTeams.stream().anyMatch(t ->
                (t.getPlayerA() != null && t.getPlayerA().getId().equals(player.getId())
                        || t.getPlayerB() != null && t.getPlayerB().getId().equals(player.getId())))
        )
            throw new WebApplicationException("Player already registered", Response.Status.CONFLICT);
    }

    private void checkRoleAndAccessibility(Competition competition) {
        if (!securityIdentity.hasRole("director") && // or registration phase
                (competition.getTournament().getBeginRegistration().isAfter(Instant.now())
                        || competition.getTournament().getEndRegistration().isBefore(Instant.now())))
            throw new NotAuthorizedException("Registration phase is not active");
    }

    private boolean conditionsFailA(Competition comp, Player player) {
        return (comp.getPlayerASex() == SexFilter.FEMALE && player.getSex() == Sex.MALE)
                || (comp.getPlayerASex() == SexFilter.MALE && player.getSex() == Sex.FEMALE)
                || (comp.playerAhasMinAge() && comp.getPlayerAminAge().isBefore(player.getBirthday()))
                || (comp.playerAhasMaxAge() && comp.getPlayerAmaxAge().isAfter(player.getBirthday()));
    }

    private boolean conditionsFailB(Competition comp, Player player) {
        return (comp.getPlayerBSex() == SexFilter.FEMALE && player.getSex() == Sex.MALE)
                || (comp.getPlayerBSex() == SexFilter.MALE && player.getSex() == Sex.FEMALE)
                || (comp.playerBhasMinAge() && comp.getPlayerBminAge().isBefore(player.getBirthday()))
                || (comp.playerBhasMaxAge() && comp.getPlayerBmaxAge().isAfter(player.getBirthday()));
    }
}
