package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.enums.*;
import de.secretj12.turnierplaner.db.entities.competition.*;
import de.secretj12.turnierplaner.db.entities.groups.Group;
import de.secretj12.turnierplaner.db.repositories.*;
import de.secretj12.turnierplaner.logic.CompetitionLogic;
import de.secretj12.turnierplaner.mails.MailTemplates;
import de.secretj12.turnierplaner.model.director.competition.jDirectorCompetitionAdd;
import de.secretj12.turnierplaner.model.director.competition.jDirectorCompetitionUpdate;
import de.secretj12.turnierplaner.model.director.competition.jDirectorGroupsDivision;
import de.secretj12.turnierplaner.model.director.jDirectorScheduleMatch;
import de.secretj12.turnierplaner.model.user.competition.jUserCompetition;
import de.secretj12.turnierplaner.model.user.group.jUserGroupSystem;
import de.secretj12.turnierplaner.model.user.jUserPlayerSignUpForm;
import de.secretj12.turnierplaner.model.user.jUserTeam;
import de.secretj12.turnierplaner.model.user.knockout.jUserKnockoutMatch;
import de.secretj12.turnierplaner.model.user.knockout.jUserKnockoutSystem;
import de.secretj12.turnierplaner.tools.CommonHelpers;
import de.secretj12.turnierplaner.tools.GroupTools;
import de.secretj12.turnierplaner.tools.KnockoutTools;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/tournament/{tourName}/competition")
public class CompetitionResource {
    // TODO always check for current progress
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
    @Inject
    CompetitionLogic competitionLogic;

    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<jUserCompetition> getAllCompetitions(@PathParam("tourName") String tourName) {
        common.checkTournamentAccessibility(tourName);
        return competitions.listByName(tourName).stream().map(jUserCompetition::new).toList();
    }

    @GET
    @Path("/prepare")
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<jDirectorCompetitionUpdate> getPrepareCompetitions(@PathParam("tourName") String tourName) {
        return competitions.listByName(tourName).stream().map(jDirectorCompetitionUpdate::new).toList();
    }

    @GET
    @Path("/{compName}/details")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public jUserCompetition getCompetition(@PathParam("tourName") String tourName,
                                           @PathParam("compName") String compName) {
        common.checkTournamentAccessibility(tourName);

        if (securityIdentity.hasRole("director"))
            return new jDirectorCompetitionUpdate(competitions.getByName(tourName, compName));
        return new jUserCompetition(competitions.getByName(tourName, compName));
    }

    @GET
    @Path("/canEdit")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean canEdit(@PathParam("tourName") String tourName) {
        return securityIdentity.hasRole("director");
    }

    @POST
    @Path("/add")
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addCompetition(@PathParam("tourName") String tourName, jDirectorCompetitionAdd competition) {
        competitionLogic.addCompetitionLogic(tourName, competition);
        return "successfully added";
    }

    @POST
    @Path("/update")
    @RolesAllowed("director")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateCompetition(@PathParam("tourName") String tourName, jDirectorCompetitionUpdate competition) {
        Competition dbCompetition = competitions.findById(competition.getId());
        if (dbCompetition == null) throw new BadRequestException("Competition doesn't exist");
        if (!dbCompetition.getTournament().getName().equals(tourName))
            throw new BadRequestException("Tournament of competition is not the given");

        competition.toDB(dbCompetition);
        competitions.persist(dbCompetition);

        // TODO update players (remove all not fitting, warn in frontend if conditions are changed!)

        return "successfully changed";
    }

    @GET
    @Path("/{compName}/signedUpTeams")
    @Produces(MediaType.APPLICATION_JSON)
    public List<jUserTeam> getSignedUpPlayers(@PathParam("tourName") String tourName,
                                              @PathParam("compName") String compName) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new NotFoundException("Competition was not found");

        if (!securityIdentity.hasRole("director")
            && (competition.getTournament().getBeginRegistration().isAfter(Instant.now())
                || competition.getTournament().getBeginGamePhase().isBefore(Instant.now())))
            throw new NotAuthorizedException("Registration phase is not active");

        return competition.getTeams().stream().map(jUserTeam::new).toList();
    }

    @POST
    @Path("/{compName}/signUp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String signUpPlayer(@PathParam("tourName") String tourName, @PathParam("compName") String compName,
                               jUserPlayerSignUpForm reg) {
        competitionLogic.signUpPlayerLogic(tourName, compName, reg);
        return "Player registered";
    }

    @POST
    @Path("/{compName}/deleteTeam")
    @Transactional
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteTeam(@PathParam("tourName") String tourName, @PathParam("compName") String compName,
                             jUserTeam team) {
        Team dbTeam = teams.findById(team.getId());
        if (dbTeam == null)
            throw new BadRequestException("Team does not exist");
        Competition competition = competitions.getByName(tourName, compName);
        switch (competition.getcProgress()) {
            case GAMES, SCHEDULING:
                throw new BadRequestException("Can't delete team after assigning teams");
        }
        teams.delete(dbTeam);
        return "Team deleted";
    }

    @POST
    @Transactional
    @RolesAllowed("director")
    @Path("/{compName}/updateTeams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateTeams(@PathParam("tourName") String tourName, @PathParam("compName") String compName,
                              List<jUserTeam> teams) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new BadRequestException("Competition doesn't exist");

        List<Team> curTeams = competition.getTeams();
        for (var cTeam : curTeams) {
            var teamOp = teams.stream().filter(cT -> cTeam.getId().equals(cT.getId())).findAny();
            if (teamOp.isEmpty()) {
                this.teams.delete(cTeam);
            }
        }
        for (var team : teams) {
            Player playerA = team.getPlayerA() == null ? null : players.findById(team.getPlayerA().getId());
            Player playerB = team.getPlayerB() == null ? null : players.findById(team.getPlayerB().getId());

            var cTeamOp = curTeams.stream().filter(cT -> cT.getId().equals(team.getId())).findAny();
            if (cTeamOp.isEmpty()) {
                Team t = new Team();
                t.setCompetition(competition);
                t.setPlayerA(playerA);
                t.setPlayerB(playerB);
                this.teams.persist(t);
            } else {
                var cTeam = cTeamOp.get();
                cTeam.setPlayerA(playerA);
                cTeam.setPlayerB(playerB);
                this.teams.persist(cTeam);
            }
        }

        competition.setcProgress(CreationProgress.GAMES);
        competitions.persist(competition);

        return "Teams updated";
    }

    @GET
    @Path("/{compName}/knockoutMatches")
    @Produces(MediaType.APPLICATION_JSON)
    public jUserKnockoutSystem getKnockoutMatches(@PathParam("tourName") String tourName,
                                                  @PathParam("compName") String compName) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new NotFoundException("Could not find competition");
        if (competition.getType() != CompetitionType.KNOCKOUT)
            throw new WebApplicationException("Competition does not have type knockout", Response.Status.METHOD_NOT_ALLOWED);

        Match finale = competition.getFinale();
        Match thirdPlace = competition.getThirdPlace();
        return new jUserKnockoutSystem(finale, thirdPlace, securityIdentity.hasRole("director"));
    }

    @GET
    @Path("/{compName}/groupMatches")
    @Produces(MediaType.APPLICATION_JSON)
    public jUserGroupSystem getGroupMatches(@PathParam("tourName") String tourName,
                                            @PathParam("compName") String compName) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new NotFoundException("Competition as not found");
        if (competition.getType() != CompetitionType.GROUPS)
            throw new NotAllowedException("Competition does not have type groups");

        List<Group> groups = competition.getGroups();
        if (groups == null) throw new NotFoundException("Found no groups");

        Match finale = competition.getFinale();
        Match thirdPlace = competition.getThirdPlace();
        if (groups.size() > 2 && (finale == null || thirdPlace == null))
            throw new InternalServerErrorException("Finale or thirdPlace is null");
        return new jUserGroupSystem(groups, finale, thirdPlace, groupTools);
    }

    @POST
    @Transactional
    @RolesAllowed("director")
    @Path("/{compName}/initKnockout")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean initializeMatchesKnockout(@PathParam("tourName") String tourName,
                                             @PathParam("compName") String compName, jUserKnockoutMatch tree) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);

        knockoutTools.updateKnockoutTree(competition, tree);

        competition.setcProgress(CreationProgress.SCHEDULING);
        competitions.persist(competition);
        return true;
    }

    @POST
    @Transactional
    @RolesAllowed("director")
    @Path("/{compName}/initGroups")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean initializeMatchesGroups(@PathParam("tourName") String tourName,
                                           @PathParam("compName") String compName, jDirectorGroupsDivision division) {
        if (division.getGroups().stream().anyMatch(g -> g.size() <= 1))
            throw new BadRequestException("At least 2 groups per team needed");

        common.checkTournamentAccessibility(tourName);

        // @formatter:off
        List<Set<Team>> groups = division.getGroups().stream()
            .map(group -> group.stream()
                .map(t -> teams.findById(t.getId()))
                .collect(Collectors.toCollection(HashSet::new)))
            .collect(Collectors.toList());
        // @formatter:on

        if (groups.stream().anyMatch(group -> group.stream().anyMatch(Objects::isNull)))
            throw new NotFoundException("Player not found");

        checkGroupsCount(groups.size());

        Competition competition = competitions.getByName(tourName, compName);

        groupTools.generateMatches(competition, groups);

        competition.setcProgress(CreationProgress.SCHEDULING);
        competitions.persist(competition);
        return true;
    }

    private void checkGroupsCount(int count) {
        int i = 1;
        while (i <= 1024) {
            if (i == count)
                return;
            i *= 2;
        }
        throw new BadRequestException("Invalid group count");
    }

    @GET
    @Transactional
    @RolesAllowed("director")
    @Path("/{compName}/groupsDivision")
    @Produces(MediaType.APPLICATION_JSON)
    public jDirectorGroupsDivision groupsDivision(@PathParam("tourName") String tourName,
                                                  @PathParam("compName") String compName) {
        common.checkTournamentAccessibility(tourName);

        Competition competition = competitions.getByName(tourName, compName);
        List<Group> groups = competition.getGroups();
        return new jDirectorGroupsDivision(groups.stream().map(g -> groupTools.teamsOfGroup(g)).toList());
    }

    @POST
    @Path("/{compName}/match")
    @Transactional
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateMatches(@PathParam("tourName") String tourName, @PathParam("compName") String compName,
                                List<jDirectorScheduleMatch> matches) {
        Competition competition = competitions.getByName(tourName, compName);
        if (competition == null) throw new NotFoundException("Competition could not be found");

        for (var cMatch : matches) {
            Match match = this.matches.findById(cMatch.getId());
            if (match == null)
                throw new NotFoundException("Could not find match");
            if (match.getCompetition().getId() != competition.getId())
                throw new BadRequestException("Match does not belong to specified competition");
            match.setCourt(courts.findByName(cMatch.getCourt()));
            match.setBegin(cMatch.getBegin());
            match.setEnd(cMatch.getEnd());

            this.matches.persist(match);
        }
        return "Updated matches";
    }
}
