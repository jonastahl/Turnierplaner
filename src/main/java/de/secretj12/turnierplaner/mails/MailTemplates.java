package de.secretj12.turnierplaner.mails;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateContents;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.DateFormat;
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
                                                                                        String firstName,
                                                                                        String compName,
                                                                                        boolean multipleComps,
                                                                                        boolean multipleMatches,
                                                                                        List<PlayersCompetitions> competitions
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
        if (player.isMailVerified())
            Templates.createCompetitionRegistrationMail(url, player.getFirstName(), player.getEmail(), competition
                .getName())
                .setAttribute("locale", player.getLanguage().getLanguageCode())
                .to(player.getEmail())
                .subject(new registrationSubject().setLocale(player.getLanguage().getLanguageCode()).render())
                .sendAndAwait();
    }

    record PlayersMatches(String opponent, String time, String court) {
    }

    record PlayersCompetitions(String name, List<PlayersMatches> games) {
    }

    private String getOpponentTeam(Player player, Match match) {
        if (match.getTeamA().getPlayerA().getId() == player.getId()
            || (match.getTeamA().getPlayerB() != null && match.getTeamA().getPlayerB().getId() == player.getId())
        ) {
            return match.getTeamB().getFullName();
        } else if (match.getTeamB().getPlayerA().getId() == player.getId()
            || (match.getTeamB().getPlayerB() != null && match.getTeamB().getPlayerB().getId() == player.getId())) {
                return match.getTeamA().getFullName();
            } else {
                throw new IllegalStateException("Player is not the match");
            }
    }

    public boolean sendPublishedMail(Player player, Map<Competition, List<Match>> playersMatches) {
        if (!player.isMailVerified() || player.getEmail() == null) return false;

        Locale loc = new Locale.Builder().setLanguage(player.getLanguage().getLanguageCode()).build();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, loc);

        String compName = String.join(", ", playersMatches.keySet().stream().map(Competition::getName).toList());
        boolean multipleComps = playersMatches.size() > 1;
        boolean multipleMatches = multipleComps || playersMatches.values().stream().mapToInt(List::size).sum() > 1;
        // @formatter:off
        List<PlayersCompetitions> playerCompetitions = playersMatches.entrySet().stream().map(
            entry -> new PlayersCompetitions(
                                             entry.getKey().getName(), entry.getValue().stream().map(
                                                 m -> new PlayersMatches(
                                                                         getOpponentTeam(player, m),
                                                                         dateFormat.format(Date.from(m.getBegin())),
                                                                         m.getCourt().getName())
                                             ).toList()
            )
        ).toList();
        // @formatter:on

        Templates.createPublishPreparation(
            player.getFirstName(),
            compName,
            multipleComps,
            multipleMatches,
            playerCompetitions)
            .setAttribute("locale", player.getLanguage().getLanguageCode())
            .to(player.getEmail())
            .subject(new registrationSubject().setLocale(player.getLanguage().getLanguageCode()).render())
            .sendAndAwait();

        return true;
    }
}
