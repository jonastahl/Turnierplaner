package de.secretj12.turnierplaner.cache;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class BackupDownloadTokenCache extends DownloadTokenCache<Boolean> {

    public UUID generateDownloadToken() {
        return super.generateDownloadToken(false);
    }
}
