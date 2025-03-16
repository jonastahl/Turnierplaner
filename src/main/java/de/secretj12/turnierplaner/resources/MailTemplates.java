package de.secretj12.turnierplaner.resources;

import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MailTemplates {
    // TODO internationalization

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

        public static native MailTemplate.MailTemplateInstance createRegistrationMail(
                                                                                      String url,
                                                                                      String firstName,
                                                                                      String recipient,
                                                                                      String competitionName,
                                                                                      String competitionDescription
        );
    }


    public void verificationMail(Player player, String verificationCode) {
        Templates.createVerificationMail(url, player.getFirstName(), player.getEmail(), verificationCode)
            .to(player.getEmail())
            .subject("Bitte verifizieren Sie Ihre E-Mail")
            .sendAndAwait();
    }

    public void sendRegistrationMail(Player player, Competition competition) {
        if (player.isMailVerified())
            Templates.createRegistrationMail(url, player.getFirstName(), player.getEmail(), competition.getName(),
                competition.getDescription())
                .to(player.getEmail())
                .subject("Registrierung bei Turnierplaner")
                .sendAndAwait();
    }
}
