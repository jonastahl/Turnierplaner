package de.secretj12.turnierplaner.model.user;

import de.secretj12.turnierplaner.db.entities.Config;
import de.secretj12.turnierplaner.db.entities.DefaultConfig;

public class jUserConfig {
    private String title;
    private String language;
    private boolean adminVerificationNeeded;
    private String auth_url;
    private String legalNotice;
    private String privacyPolicy;

    public jUserConfig() {
    }

    public jUserConfig(DefaultConfig defConfig, Config config, String auth_config, String legalNotice,
                       String privacyPolicy) {
        this.title = defConfig.getTitle() != null ? defConfig.getTitle() : "title";
        if (config == null)
            this.language = defConfig.getLanguage();
        else
            this.language = config.getLanguage();
        this.adminVerificationNeeded = defConfig.isAdminVerificationNeeded();
        this.auth_url = auth_config;
        this.legalNotice = legalNotice;
        this.privacyPolicy = privacyPolicy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isAdminVerificationNeeded() {
        return adminVerificationNeeded;
    }

    public void setAdminVerificationNeeded(boolean adminVerificationNeeded) {
        this.adminVerificationNeeded = adminVerificationNeeded;
    }

    public String getAuth_url() {
        return auth_url;
    }

    public void setAuth_url(String auth_url) {
        this.auth_url = auth_url;
    }

    public String getLegalNotice() {
        return legalNotice;
    }

    public void setLegalNotice(String legalNotice) {
        this.legalNotice = legalNotice;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }
}
