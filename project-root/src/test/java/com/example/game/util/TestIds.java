package com.example.game.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestIds {
    private static final String RUN_ID = initRunId();
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private TestIds() {
    }

    private static String initRunId() {
        String env = System.getenv("RUN_ID");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String uniqueSuffix() {
        return RUN_ID + "_" + COUNTER.incrementAndGet();
    }

    public static String username(String prefix) {
        return prefix + "_" + uniqueSuffix();
    }

    public static String email(String prefix) {
        return prefix + "_" + uniqueSuffix() + "@example.com";
    }

    public static long uniqueLongId() {
        long base = Math.abs((long) RUN_ID.hashCode()) * 1000L;
        return base + COUNTER.incrementAndGet();
    }

    public static int highScore() {
        int delta = Math.abs(COUNTER.incrementAndGet() % 1000);
        return 2_000_000_000 - delta;
    }
}
