package ru.secureoverlay.secure_overlay.security;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayGuard {

    private final ConcurrentHashMap<UUID, Long> seen = new ConcurrentHashMap<>();


    private final long ttlMillis;


    public ReplayGuard(Duration ttl
    ) {

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {

            throw new IllegalArgumentException("Replay TTL must be positive");
        }

        this.ttlMillis = ttl.toMillis();
    }


    public boolean accept(UUID requestId) {

        if (requestId == null) {
            return false;
        }


        cleanup();


        long now = System.currentTimeMillis();


        Long previous = seen.putIfAbsent(
                        requestId,
                        now
                );


        return previous == null;
    }


    private void cleanup() {

        long threshold = System.currentTimeMillis() - ttlMillis;


        seen.entrySet().removeIf(entry -> entry.getValue() < threshold);
    }


    public int size() {return seen.size();}
}