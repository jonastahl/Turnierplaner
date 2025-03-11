package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.DefaultConfig;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.enums.Sex;
import de.secretj12.turnierplaner.db.entities.VerificationCode;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.repositories.CompetitionRepository;
import de.secretj12.turnierplaner.db.repositories.DefaultConfigRepository;
import de.secretj12.turnierplaner.db.repositories.PlayerRepository;
import de.secretj12.turnierplaner.db.repositories.VerificationCodeRepository;
import de.secretj12.turnierplaner.model.director.jDirectorPlayer;
import de.secretj12.turnierplaner.model.director.jDirectorPlayerUpdateForm;
import de.secretj12.turnierplaner.model.jPage;
import de.secretj12.turnierplaner.model.user.jUserPlayer;
import de.secretj12.turnierplaner.model.user.jUserPlayerRegistrationForm;
import de.secretj12.turnierplaner.tools.CommonHelpers;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Path("/player")
public class PlayerResource {
    private static final Logger log = LoggerFactory.getLogger(PlayerResource.class);
    @Inject
    CompetitionRepository competitionRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    VerificationCodeRepository verificationCodeRepository;
    @Inject
    Mailer mailer;
    @Inject
    MailTemplates mailTemplates;

    @Inject
    DefaultConfigRepository defaultConfigRepository;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    CommonHelpers commonHelpers;

    @ConfigProperty(name = "turnierplaner.registration.expire")
    public int expire;

    @GET
    @Path("/compFind/{tourId}/{compId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<jUserPlayer> listCompPlayer(@PathParam("tourId") String tourId, @PathParam("compId") String compId,
                                            @DefaultValue("") @QueryParam("search") String search,
                                            @DefaultValue("false") @QueryParam("playerB") boolean playerB) {
        commonHelpers.checkTournamentAccessibility(tourId);
        Competition competition = competitionRepository.getByName(tourId, compId);
        if (competition == null)
            throw new BadRequestException("Invalid competition");

        LocalDate minAge = playerB ? (competition.playerBhasMinAge() ? competition
            .getPlayerBminAge() : null) : (competition.playerAhasMinAge() ? competition.getPlayerAminAge() : null);
        LocalDate maxAge = playerB ? (competition.playerBhasMaxAge() ? competition
            .getPlayerBmaxAge() : null) : (competition.playerAhasMaxAge() ? competition.getPlayerAmaxAge() : null);

        Sex dbSex = switch (playerB ? competition.getPlayerBSex() : competition.getPlayerASex()) {
            case MALE -> Sex.MALE;
            case FEMALE -> Sex.FEMALE;
            case ANY -> null;
        };
        if (search.isEmpty()) {
            return List.of();
        }

        DefaultConfig defConfig = defaultConfigRepository.findById(0L);
        boolean admin = securityIdentity.hasRole("director") || !defConfig.isAdminVerificationNeeded();
        return playerRepository.filter(search, dbSex, minAge, maxAge, admin, 0, 10).map(jUserPlayer::new).toList();
    }

    @GET
    @Path("/find")
    @RolesAllowed("director")
    @Produces(MediaType.APPLICATION_JSON)
    public jPage<List<jDirectorPlayer>> listPlayer(@QueryParam("search") String search,
                                                   @DefaultValue("0") @QueryParam("page") int page,
                                                   @DefaultValue("5") @QueryParam("pageSize") int pageSize) {
        if (pageSize <= 0)
            throw new BadRequestException("Page size has to be greater 0");
        return new jPage<>(
                           playerRepository.countFilter(search, null, null, null, true), playerRepository.filter(search,
                               null, null, null, true, page, pageSize)
                               .map(jDirectorPlayer::new).toList());
    }

    @GET
    @Path("/listUnverified")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("director")
    public List<jUserPlayer> listUnverified() {
        return playerRepository.adminUnverified().map(jUserPlayer::new).toList();
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public jUserPlayer getPlayer(@PathParam("playerId") UUID playerId) {
        Player player = playerRepository.findById(playerId);
        if (player == null)
            throw new NotFoundException("Player not found");
        return new jUserPlayer(player);
    }

    @GET
    @Path("/{playerId}/details")
    @RolesAllowed("director")
    @Produces(MediaType.APPLICATION_JSON)
    public jDirectorPlayerUpdateForm getDetails(@PathParam("playerId") UUID playerId) {
        Player player = playerRepository.findById(playerId);
        if (player == null)
            throw new NotFoundException("Player not found");
        return new jDirectorPlayerUpdateForm(player);
    }

    private void checkPlayerForm(jUserPlayerRegistrationForm form) {
        if (form.getFirstName() == null)
            throw new BadRequestException("First name may not be empty");
        if (form.getLastName() == null)
            throw new BadRequestException("Last name may not be empty");
        if (form.getBirthday() == null)
            throw new BadRequestException("Birthday may not be empty");
        if (form.getEmail() == null)
            throw new BadRequestException("E-Mail may not be empty");
        if (form.getPhone() == null)
            throw new BadRequestException("Phone may not be empty");
    }

    private void updatePlayer(jUserPlayerRegistrationForm form, Player player) {
        player.setFirstName(form.getFirstName());
        player.setLastName(form.getLastName());
        player.setBirthday(form.getBirthday());
        if (form.getSex() == null) throw new BadRequestException("Sex is null");
        switch (form.getSex()) {
            case MALE -> player.setSex(Sex.MALE);
            case FEMALE -> player.setSex(Sex.FEMALE);
        }
        player.setEmail(form.getEmail());
        player.setPhone(form.getPhone());
    }

    @POST
    @Transactional
    @Path("/registration")
    @Blocking
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String playerRegistration(jUserPlayerRegistrationForm playerForm) {
        Player exPlayer = playerRepository.getByName(playerForm.getFirstName(), playerForm.getLastName());
        if (exPlayer != null &&
            (exPlayer.getVerificationCode() == null
                || (exPlayer.getVerificationCode() != null && exPlayer.getVerificationCode().getExpirationDate()
                    .isAfter(Instant.now()))))
            throw new WebApplicationException("A player already exists with this name", Response.Status.CONFLICT);

        checkPlayerForm(playerForm);

        Player newPlayer = new Player();
        updatePlayer(playerForm, newPlayer);
        newPlayer.setMailVerified(securityIdentity.hasRole("director"));
        newPlayer.setAdminVerified(securityIdentity.hasRole("director"));
        playerRepository.persist(newPlayer);

        System.out.println("Player: " + newPlayer.getId());
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPlayer(newPlayer);
        verificationCode.setExpirationDate(Instant.now().plus(expire, ChronoUnit.MINUTES));
        verificationCodeRepository.persist(verificationCode);


        try {
            mailTemplates.verificationMail(newPlayer, verificationCode.getId()
                .toString());
        } catch (Exception e) {
            // TODO print by logger
            log.error("e: ", e);
            throw new BadRequestException("Problem sending you the verification mail. Please try again later.");
        }

        return "successfully added";
    }

    @POST
    @Transactional
    @Path("/update")
    @Blocking
    @RolesAllowed("director")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String playerRegistration(jDirectorPlayerUpdateForm uPlayer) {
        Player player = playerRepository.findById(uPlayer.getId());
        if (player == null)
            throw new BadRequestException("Player does not exist");

        checkPlayerForm(uPlayer);

        updatePlayer(uPlayer, player);
        player.setAdminVerified(true);
        playerRepository.persist(player);

        return "successfully updated";
    }

    @GET
    @Transactional
    @Path("/verification")
    @Produces(MediaType.TEXT_PLAIN)
    public String verification(@QueryParam("code") String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByUUID(UUID.fromString(code));
        if (verificationCode == null) throw new BadRequestException("This verification code is not correct!");

        Player player = verificationCode.getPlayer();
        player.setMailVerified(true);
        player.setVerificationCode(null);
        playerRepository.persist(player);
        verificationCodeRepository.delete(verificationCode);
        return "Successfully verified!";
    }

    @POST
    @RolesAllowed("director")
    @Transactional
    @Path("/adminVerify")
    @Produces(MediaType.TEXT_PLAIN)
    public String adminVerify(Player player) {
        Player dbPlayer = playerRepository.findById(player.getId());
        dbPlayer.setAdminVerified(true);
        playerRepository.persist(dbPlayer);
        return "Successfully verified!";
    }

    @POST
    @RolesAllowed("director")
    @Transactional
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deletePlayer(Player player) {
        Player dbPlayer = playerRepository.findById(player.getId());
        playerRepository.delete(dbPlayer);
        return "Successfully verified!";
    }

    @Transactional
    @Scheduled(every = "30m", identity = "clear-verification-code")
    void clear() {
        List<VerificationCode> codes = verificationCodeRepository.find(
            "FROM VerificationCode v WHERE v.expiration_date < " + "current_timestamp()").list();
        for (var code : codes) {
            if (code.getPlayer() != null)
                playerRepository.delete(code.getPlayer());
            verificationCodeRepository.delete(code);
        }
    }
}