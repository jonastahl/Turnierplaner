package de.secretj12.turnierplaner.mails;

import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface MailMessages {

    String helloName(String name);

    String registerSubject();

    String registeredFor(String compName, String compDescription);

    String verifySubject();

    String verifyMail();

    String verifyAccept();

    String verifyIgnoreInvalid();

    String thanksForUsing();
}
