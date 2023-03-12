package de.secretj12.tournierplaner.resources;

import de.secretj12.tournierplaner.entities.Competition;
import de.secretj12.tournierplaner.entities.Tournament;
import de.secretj12.tournierplaner.repositories.CompetitionRepository;
import de.secretj12.tournierplaner.repositories.TournamentRepository;
import io.quarkus.security.identity.SecurityIdentity;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/competition")
public class CompetitionResource {

    @Inject
    CompetitionRepository competitions;
    @Inject
    TournamentRepository tournaments;
    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Competition> getAllCompetitions(@QueryParam("tourName") String tourName) {
        if (canSee(tourName))
            return competitions.listByName(tourName);
        return null;
    }

    @GET
    @Path("/details")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Competition getCompetition(@QueryParam("tourName") String tourName, @QueryParam("compName") String compName) {
        if (canSee(tourName))
            return competitions.getByName(tourName, compName);
        return null;
    }

    @GET
    @Path("/canEdit")
    @Produces(MediaType.TEXT_PLAIN)
    public Response canEdit() {
        if (securityIdentity.hasRole("director"))
            return Response.ok("Authorized").build();
        else
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    }

    @POST
    @Path("/add")
    @RolesAllowed("director")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addCompetition(Competition competition) {
        if (competition.getTournament() == null)
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                    "No tournament specified").build();
        if (competitions.getByName(competition.getTournament().getName(), competition.getName()) != null)
            return Response.status(Response.Status.CONFLICT).build();
        Tournament tournament = tournaments.getByName(competition.getTournament().getName());
        if (tournament == null)
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                    "Tournament doesn't exist").build();

        competition.setTournament(tournament);
        competitions.persist(competition);
        return Response.ok("successfully added").build();
    }

    @POST
    @Path("/update")
    @RolesAllowed("director")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateCompetition(Competition competition) {
        Competition savedCompetition = competitions.getById(competition.getId());
        if (savedCompetition == null)
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                    "Competition doesn't exist").build();

        competitions.persist(savedCompetition);
        savedCompetition.setName(competition.getName());
        savedCompetition.setDescription(competition.getDescription());
        savedCompetition.setType(competition.getType());

        return Response.ok("successfully changed").build();
    }

    private boolean canSee(String tourName) {
        return securityIdentity.hasRole("director") || tournaments.getByName(tourName).isVisible();
    }
}
