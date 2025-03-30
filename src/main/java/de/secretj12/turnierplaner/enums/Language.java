package de.secretj12.turnierplaner.enums;

public enum Language {
    EN("en"), DE("de");

    private final String languageName;

    Language(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageName;
    }
}
