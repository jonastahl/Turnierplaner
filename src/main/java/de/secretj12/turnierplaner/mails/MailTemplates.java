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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.DateFormat;
import java.util.*;

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
                                                                                      List<PlayersCompetitions<PlayersMatchesDateLocUpdate>> competitions
        );

        public static native MailTemplate.MailTemplateInstance createMatchUpdate(
                                                                                 String url,
                                                                                 Player player,
                                                                                 String tourName,
                                                                                 String compName,
                                                                                 boolean multipleUpdate,
                                                                                 List<PlayersCompetitions<PlayersMatchesUpdate>> competitionsChange,
                                                                                 boolean multipleAdded,
                                                                                 List<PlayersCompetitions<PlayersMatches>> competitionsAdded
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

    record PlayersMatchesDateLocUpdate(Player opponentA, Player opponentB, String time, String court, String oldTime,
                                       String oldCourt) {
    }

    record PlayersMatchesUpdate(Player opponentA, Player opponentB, String time, String court, String result,
                                Player oldOpponentA, Player oldOpponentB, String oldTime, String oldCourt,
                                String oldResult) {
    }

    record PlayersCompetitions<T>(String name, List<T> games) {
    }

    private boolean isTeamA(Player player, Match match) {
        if (match.getTeamA() != null && (match.getTeamA().getPlayerA().getId() == player.getId()
            || (match.getTeamA().getPlayerB() != null && match.getTeamA().getPlayerB().getId() == player.getId()))
        ) {
            return false;
        } else if (match.getTeamB() != null && (match.getTeamB().getPlayerA().getId() == player.getId()
            || (match.getTeamB().getPlayerB() != null && match.getTeamB().getPlayerB().getId() == player.getId()))) {
                return true;
            } else {
                throw new IllegalStateException("Player is not the match");
            }
    }

    private Team getOpponentTeam(Player player, Match match) {
        return isTeamA(player, match) ? match.getTeamA() : match.getTeamB();
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

    public boolean sendRescheduleMail(Player player, String tourName,
                                      Map<Competition, List<Tuple2<Match, Match>>> playersMatches) {
        if (!player.isMailVerified() || player.getEmail() == null) return false;

        Locale loc = new Locale.Builder().setLanguage(player.getLanguage().getLanguageCode()).build();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, loc);

        String compName = String.join(", ", playersMatches.keySet().stream().map(Competition::getName).toList());
        boolean multipleMatches = playersMatches.values().stream().mapToInt(List::size).sum() > 1;
        // @formatter:off
        List<PlayersCompetitions<PlayersMatchesDateLocUpdate>> playerCompetitions = playersMatches.entrySet().stream().map(
            entry -> new PlayersCompetitions<>(
                entry.getKey().getName(), entry.getValue().stream().map(
                data -> new PlayersMatchesDateLocUpdate(
                    getOpponentTeam(player, data.getItem1()).getPlayerA(),
                    getOpponentTeam(player, data.getItem1()).getPlayerB(),
                    dateFormat.format(Date.from(data.getItem1().getBegin())),
                    data.getItem1().getCourt().getName(),
                    data.getItem2().getBegin().equals(data.getItem1().getBegin()) ?
                        null : dateFormat.format(Date.from(data.getItem2().getBegin())),
                    data.getItem2().getCourt().getName().equals(data.getItem1().getCourt().getName()) ?
                        null : data.getItem2().getCourt().getName())
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
            .send().subscribe().with(unused -> {
            });

        return true;
    }

    @TemplateContents("{msg:matchUpdateSubject(compName)}")
    record matchUpdateSubject(String compName) implements TemplateInstance {
    }

    private String printResult(Match match, boolean reversed) {
        return String.join(" / ", match.getSets().stream()
            .map(s -> reversed ? (s.getScoreB() + "-" + s.getScoreA()) : (s.getScoreA() + "-" + s.getScoreB()))
            .toList());
    }

    public boolean sendMatchUpdateMail(Player player, String tourName, String compName,
                                       List<Tuple2<Match, Match>> matchesUpdate, List<Match> matchesAdded) {
        if (!player.isMailVerified() || player.getEmail() == null) return false;

        Locale loc = new Locale.Builder().setLanguage(player.getLanguage().getLanguageCode()).build();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, loc);

        boolean multipleUpdate = matchesUpdate.size() > 1;
        // @formatter:off
        PlayersCompetitions<PlayersMatchesUpdate> compMatchesUpdate = new PlayersCompetitions<>(compName, matchesUpdate.stream().map(
            data -> new PlayersMatchesUpdate(
                Optional.ofNullable(getOpponentTeam(player, data.getItem1())).map(Team::getPlayerA).orElse(null),
                Optional.ofNullable(getOpponentTeam(player, data.getItem1())).map(Team::getPlayerB).orElse(null),
                dateFormat.format(Date.from(data.getItem1().getBegin())),
                data.getItem1().getCourt().getName(),
                String.join(" / ", data.getItem1().getSets().stream().map(s -> s.getScoreA() + "-" + s.getScoreB()).toList()),
                Optional.ofNullable(getOpponentTeam(player, data.getItem2())).map(Team::getPlayerA).orElse(null),
                Optional.ofNullable(getOpponentTeam(player, data.getItem2())).map(Team::getPlayerB).orElse(null),
                dateFormat.format(Date.from(data.getItem2().getBegin())),
                data.getItem2().getCourt().getName(),
                String.join(" / ", data.getItem2().getSets().stream().map(s -> s.getScoreA() + "-" + s.getScoreB()).toList())
           )
        ).toList());
        // @formatter:on

        boolean multipleAdded = matchesAdded.size() > 1;
        // @formatter:off
        PlayersCompetitions<PlayersMatches> compMatchesAdded = new PlayersCompetitions<>(compName,
            matchesAdded.stream().map(
            m -> new PlayersMatches(
                Optional.ofNullable(getOpponentTeam(player, m)).map(Team::getPlayerA).orElse(null),
                Optional.ofNullable(getOpponentTeam(player, m)).map(Team::getPlayerB).orElse(null),
                dateFormat.format(Date.from(m.getBegin())),
                m.getCourt().getName())
            ).toList()
        );
        // @formatter:on

        Templates.createMatchUpdate(
            url,
            player,
            tourName,
            compName,
            multipleUpdate,
            compMatchesUpdate.games.isEmpty() ? List.of() : List.of(compMatchesUpdate),
            multipleAdded,
            compMatchesAdded.games.isEmpty() ? List.of() : List.of(compMatchesAdded))
            .setAttribute("locale", player.getLanguage().getLanguageCode())
            .to(player.getEmail())
            .subject(new matchUpdateSubject(compName).setLocale(player.getLanguage().getLanguageCode()).render())
            .send().subscribe().with(unused -> {
            });

        return true;
    }
}
