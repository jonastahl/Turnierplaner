package de.secretj12.turnierplaner.mails;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.competition.Team;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateContents;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class MailTemplates {

    @ConfigProperty(name = "turnierplaner.frontend.url")
    String url;

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance masterTemplateMail();

        public static native MailTemplate.MailTemplateInstance createVerificationMail(
                                                                                      String url,
                                                                                      String firstName,
                                                                                      String recipient,
                                                                                      String verificationCode);

        public static native MailTemplate.MailTemplateInstance createCompetitionRegistrationMail(
                                                                                                 String url,
                                                                                                 String firstName,
                                                                                                 String recipient,
                                                                                                 String competitionName
        );

        public static native MailTemplate.MailTemplateInstance createPublishPreparation(
                                                                                        String url,
                                                                                        Player player,
                                                                                        String tourName,
                                                                                        String compName,
                                                                                        boolean multipleComps,
                                                                                        boolean multipleMatches,
                                                                                        List<PlayersCompetitions<PlayersMatches>> competitions
        );

        public static native MailTemplate.MailTemplateInstance createMatchRescheduled(
                                                                                      String url,
                                                                                      Player player,
                                                                                      String tourName,
                                                                                      String compName,
                                                                                      boolean multipleMatches,
                                                                                      List<PlayersCompetitions<PlayersMatchesUpdate>> competitions
        );
    }

    @TemplateContents("{msg:verifySubject()}")
    record verificationSubject() implements TemplateInstance {
    }

    public void verificationMail(Player player, String verificationCode) {
        Templates.createVerificationMail(url, player.getFirstName(), player.getEmail(), verificationCode)
            .setAttribute("locale", player.getLanguage().getLanguageCode())
            .to(player.getEmail())
            .subject(new verificationSubject().setLocale(player.getLanguage().getLanguageCode()).render())
            .sendAndAwait();
    }

    @TemplateContents("{msg:registerSubject()}")
    record registrationSubject() implements TemplateInstance {
    }

    public void sendRegistrationMail(Player player, Competition competition) {
        if (player.getEmail() != null && player.isMailVerified())
            Templates.createCompetitionRegistrationMail(url, player.getFirstName(), player.getEmail(), competition
                .getName())
                .setAttribute("locale", player.getLanguage().getLanguageCode())
                .to(player.getEmail())
                .subject(new registrationSubject().setLocale(player.getLanguage().getLanguageCode()).render())
                .sendAndAwait();
    }

    record PlayersMatches(Player opponentA, Player opponentB, String time, String court) {
    }

    record PlayersMatchesUpdate(Player opponentA, Player opponentB, String time, String court, String oldTime,
                                String oldCourt) {
    }

    record PlayersCompetitions<T>(String name, List<T> games) {
    }

    private Team getOpponentTeam(Player player, Match match) {
        if (match.getTeamA().getPlayerA().getId() == player.getId()
            || (match.getTeamA().getPlayerB() != null && match.getTeamA().getPlayerB().getId() == player.getId())
        ) {
            return match.getTeamB();
        } else if (match.getTeamB().getPlayerA().getId() == player.getId()
            || (match.getTeamB().getPlayerB() != null && match.getTeamB().getPlayerB().getId() == player.getId())) {
                return match.getTeamA();
            } else {
                throw new IllegalStateException("Player is not the match");
            }
    }

    @TemplateContents("{msg:preparationSubject(comps)}")
    record preparationSubject(String comps) implements TemplateInstance {
    }

    public boolean sendPublishedMail(Player player, String tourName, Map<Competition, List<Match>> playersMatches) {
        if (!player.isMailVerified() || player.getEmail() == null) return false;

        Locale loc = new Locale.Builder().setLanguage(player.getLanguage().getLanguageCode()).build();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, loc);

        String compName = String.join(", ", playersMatches.keySet().stream().map(Competition::getName).toList());
        boolean multipleComps = playersMatches.size() > 1;
        boolean multipleMatches = multipleComps || playersMatches.values().stream().mapToInt(List::size).sum() > 1;
        // @formatter:off
        List<PlayersCompetitions<PlayersMatches>> playerCompetitions = playersMatches.entrySet().stream().map(
            entry -> new PlayersCompetitions<>(
                                             entry.getKey().getName(), entry.getValue().stream().map(
                                                 m -> new PlayersMatches(
                                                                         getOpponentTeam(player, m).getPlayerA(),
                                                                         getOpponentTeam(player, m).getPlayerB(),
                                                                         dateFormat.format(Date.from(m.getBegin())),
                                                                         m.getCourt().getName())
                                             ).toList()
            )
        ).toList();
        // @formatter:on

        Templates.createPublishPreparation(
            url,
            player,
            tourName,
            compName,
            multipleComps,
            multipleMatches,
            playerCompetitions)
            .setAttribute("locale", player.getLanguage().getLanguageCode())
            .to(player.getEmail())
            .subject(new preparationSubject(compName).setLocale(player.getLanguage().getLanguageCode()).render())
            .sendAndAwait();

        return true;
    }

    @TemplateContents("{msg:rescheduleSubject()}")
    record rescheduleSubject() implements TemplateInstance {
    }

    public record OldData(Instant oldbegin, Instant oldend, String oldcourt) {
    }

    public boolean sendRescheduleMail(Player player, String tourName,
                                      Map<Competition, List<Tuple2<Match, OldData>>> playersMatches) {
        if (!player.isMailVerified() || player.getEmail() == null) return false;

        Locale loc = new Locale.Builder().setLanguage(player.getLanguage().getLanguageCode()).build();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, loc);

        String compName = String.join(", ", playersMatches.keySet().stream().map(Competition::getName).toList());
        boolean multipleMatches = playersMatches.values().stream().mapToInt(List::size).sum() > 1;
        // @formatter:off
        List<PlayersCompetitions<PlayersMatchesUpdate>> playerCompetitions = playersMatches.entrySet().stream().map(
            entry -> new PlayersCompetitions<>(
                entry.getKey().getName(), entry.getValue().stream().map(
                data -> new PlayersMatchesUpdate(
                    getOpponentTeam(player, data.getItem1()).getPlayerA(),
                    getOpponentTeam(player, data.getItem1()).getPlayerB(),
                    dateFormat.format(Date.from(data.getItem1().getBegin())),
                    data.getItem1().getCourt().getName(),
                    data.getItem2().oldbegin().equals(data.getItem1().getBegin()) ?
                        null : dateFormat.format(Date.from(data.getItem2().oldbegin())),
                    data.getItem2().oldcourt().equals(data.getItem1().getCourt().getName()) ?
                        null : data.getItem2().oldcourt())
                ).toList()
            )
        ).toList();
        // @formatter:on

        Templates.createMatchRescheduled(
            url,
            player,
            tourName,
            compName,
            multipleMatches,
            playerCompetitions)
            .setAttribute("locale", player.getLanguage().getLanguageCode())
            .to(player.getEmail())
            .subject(new rescheduleSubject().setLocale(player.getLanguage().getLanguageCode()).render())
            .sendAndAwait();

        return true;
    }
}
