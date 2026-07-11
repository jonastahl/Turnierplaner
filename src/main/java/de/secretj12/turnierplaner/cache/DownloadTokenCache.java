package de.secretj12.turnierplaner.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

abstract class DownloadTokenCache<T> {

    private final Cache<UUID, T> tokenStore = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    public UUID generateDownloadToken(T t) {
        UUID token = UUID.randomUUID();
        tokenStore.put(token, t);
        return token;
    }

    public Optional<T> consumeToken(UUID token) {
        Optional<T> t = Optional.ofNullable(tokenStore.getIfPresent(token));

        if (t.isPresent()) {
            tokenStore.invalidate(token);
        }

        return t;
    }
}

