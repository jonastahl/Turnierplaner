package de.secretj12.turnierplaner.resources;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MailTemplates {
    //    TODO internationalization

    @ConfigProperty(name = "turnierplaner.frontend.url")
    String url;

//    @CheckedTemplate
//    static class Templates {
//        public static native MailTemplate.MailTemplateInstance sendVerificationMail(String name);
//    }


    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance createVerificationMail(
                String url,
                String firstName,
                String recipient,
                String verificationCode);
    }


    public void verificationMail(String firstName, String recipient, String verificationCode) {
//        String vLink = UriBuilder
//            .fromUri(url + "/player/verification")
//            .queryParam("code", verificationCode)
//            .build()
//            .toString();
//        StringBuilder content = new StringBuilder();
//        content.append("<p>Welcome!,</p>");
//        content.append("<p>Please verify your email with the following link:</p>");
//        content.append("<a href=");
//        content.append(vLink);
//        content.append(">");
//        content.append(vLink);
//        content.append("</a>");
//        content.append("<p>Best regards</p>");
//        return Mail
//            .withHtml(recipient, "Please verify your email", content.toString());
        System.out.println(url);
        Templates.createVerificationMail(url, firstName, recipient, verificationCode)
            .to(recipient)
            .subject("Bitte verifizieren Sie Ihre E-Mail")
            .sendAndAwait();
    }
}
