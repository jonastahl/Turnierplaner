package de.secretj12.turnierplaner.resources;

import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MailTemplates {
    //    TODO internationalization

    @ConfigProperty(name = "turnierplaner.frontend.url")
    String url;

    public Mail verificationMail(String recipient, String verificationCode) {
        String vLink = UriBuilder
            .fromUri(url + "/player/verification")
            .queryParam("code", verificationCode)
            .build()
            .toString();
        StringBuilder content = new StringBuilder();
        content.append("<p>Welcome!,</p>");
        content.append("<p>Please verify your email with the following link:</p>");
        content.append("<a href=");
        content.append(vLink);
        content.append(">");
        content.append(vLink);
        content.append("</a>");
        content.append("<p>Best regards</p>");
        return Mail
            .withHtml(recipient, "Please verify your email", content.toString());
    }
}
