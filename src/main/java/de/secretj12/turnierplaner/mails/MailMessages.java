package de.secretj12.turnierplaner.mails;

import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface MailMessages {

    String helloName(String name);

    String registerSubject();

    String registeredFor(String compName);

    String verifySubject();

    String verifyMail();

    String verifyAccept();

    String verifyIgnoreInvalid();

    String thanksForUsing();

    String preparationSubject(String compName);

    String preparationDoneForSingle(String compName);

    String preparationDoneForMultiple(String compNames);

    String preparationDonePartOfSingle();

    String preparationDonePartOfMultiple();

    String upcomingGameNotice();

    String myGames();

    String rescheduleSubject();

    String rescheduleGameMovedSingle();

    String rescheduleGameMovedMultiple();
}
