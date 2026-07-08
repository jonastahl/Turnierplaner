package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Court;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.repositories.CourtRepositiory;
import de.secretj12.turnierplaner.db.repositories.TournamentRepository;
import de.secretj12.turnierplaner.model.director.jDirectorTournamentAdd;
import de.secretj12.turnierplaner.model.director.jDirectorTournamentUpdate;
import de.secretj12.turnierplaner.model.user.jUserCourt;
import de.secretj12.turnierplaner.model.user.jUserTournament;
import de.secretj12.turnierplaner.tools.CommonHelpers;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/tournament")
public class TournamentResource {

    @Inject
    TournamentRepository tournaments;
    @Inject
    CourtRepositiory courts;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    CommonHelpers common;

    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<jUserTournament> getAllTournaments() {
        if (securityIdentity.hasRole("director"))
            return tournaments.streamAll()
                .map(jDirectorTournamentAdd::new)
                .sorted(Comparator.comparing(jUserTournament::getBeginGamePhase))
                .map(t -> (jUserTournament) t).toList();
        else
            return tournaments.listAllVisible()
                .stream()
                .map(jUserTournament::new)
                .sorted(Comparator.comparing(jUserTournament::getBeginGamePhase))
                .toList();
    }

    @GET
    @Path("/{tourName}/details")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public jUserTournament getTournament(@PathParam("tourName") String name) {
        Tournament tournament = tournaments.getByName(name);
        common.checkTournamentAccessibility(tournament);
        if (securityIdentity.hasRole("director"))
            return new jDirectorTournamentUpdate(tournament);
        else
            return new jUserTournament(tournament);
    }

    @POST
    @Path("/{tourName}/updateCourts")
    @Transactional
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    public String setCourts(@PathParam("tourName") String tourName, Set<jUserCourt> tCourts) {
        Tournament tournament = tournaments.getByName(tourName);
        if (tournament == null)
            throw new NotFoundException("Could not find tournament");

        tournament.setCourts(tCourts.stream().map(court -> {
            Court dbCourt = courts.findByName(court.getName());
            if (dbCourt == null)
                throw new NotFoundException("Could not find court " + court.getName());
            return dbCourt;
        }).collect(Collectors.toSet()));
        tournaments.persist(tournament);
        return "Courts updated";
    }

    @GET
    @Path("/{tourName}/courts")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<jUserCourt> getCourts(@PathParam("tourName") String tourName) {
        Tournament tournament = tournaments.getByName(tourName);
        if (tournament == null)
            throw new NotFoundException("Could not find tournament");
        common.checkTournamentAccessibility(tournament);

        return tournament.getCourts().stream().map(jUserCourt::new).collect(Collectors.toSet());
    }

    private void checkDates(jDirectorTournamentAdd tournament) {
        if (tournament.getBeginRegistration().isAfter(tournament.getEndRegistration()))
            throw new BadRequestException("Begin of registration phase needs to be before it's end");
        if (tournament.getEndRegistration().isAfter(tournament.getBeginGamePhase()))
            throw new BadRequestException("End of registration phase needs to be before begin of game phase");
        if (tournament.getBeginGamePhase().isAfter(tournament.getEndGamePhase()))
            throw new BadRequestException("Begin of game phase needs to be before it's end");
    }

    @POST
    @Path("/add")
    @RolesAllowed("director")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addTournament(jDirectorTournamentAdd tournament) {
        if (tournament.getName() == null)
            throw new BadRequestException("Tournament name is empty");
        if (tournaments.getByName(tournament.getName()) != null)
            throw new BadRequestException("Tournament with this name already exists");
        checkDates(tournament);

        tournaments.persist(tournament.toDB());
        return "successfully added";
    }

    @POST
    @Path("/update")
    @RolesAllowed("director")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateTournament(jDirectorTournamentUpdate tournament) {
        Tournament possibleDuplicate = tournaments.getByName(tournament.getName());
        if (possibleDuplicate != null && !possibleDuplicate.getId().equals(tournament.getId()))
            throw new WebApplicationException("Tournament with this name already exist", Response.Status.CONFLICT);

        Tournament dbTournament = tournaments.findById(tournament.getId());
        if (dbTournament == null)
            throw new NotFoundException("Tournament with this id does not exist");
        checkDates(tournament);

        tournaments.findById(tournament.getId());
        dbTournament.setName(tournament.getName());
        dbTournament.setDescription(tournament.getDescription());
        dbTournament.setVisible(tournament.isVisible());
        dbTournament.setBeginRegistration(tournament.getBeginRegistration());
        dbTournament.setEndRegistration(tournament.getEndRegistration());
        dbTournament.setBeginGamePhase(tournament.getBeginGamePhase());
        dbTournament.setEndGamePhase(tournament.getEndGamePhase());
        tournaments.persist(dbTournament);
        return "successfully changed";
    }

    @DELETE
    @Path("/delete")
    @RolesAllowed("director")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteTournament(@QueryParam("tourName") String tourName) {
        System.out.println("Deleting tournament: " + tourName);
        System.err.println("Deleting tournament: " + tourName);
        Tournament tournament = tournaments.getByName(tourName);
        System.out.println("Deleting tournament: " + tourName);
        System.err.println("Deleting tournament: " + tourName);
        if (tournament == null)
            throw new NotFoundException("Tournament with this id does not exist");
        tournaments.delete(tournament);
        return "Tournament deleted successfully";
    }

}
