package de.secretj12.turnierplaner.mails;

import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateContents;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
                                                                                      String competitionName,
                                                                                      String competitionDescription
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
            Templates.createCompetitionRegistrationMail(url, player.getFirstName(), player.getEmail(), competition.getName(),
                                                        competition.getDescription())
                .setAttribute("locale", player.getLanguage().getLanguageCode())
                .to(player.getEmail())
                .subject(new registrationSubject().setLocale(player.getLanguage().getLanguageCode()).render())
                .sendAndAwait();
    }
}
