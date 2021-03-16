package com.github.onedirection.utils;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Id {
    // these fields are what's compared for equality
    // the UUID are extremely unique (2^122 possibilities)
    // and the time thing uniques it
    // It would need the same uuid to be generated in the same millis
    // for a collision to happen.
    private final String uuid;

    public static Id generateRandom() {
        return new Id(UUID.randomUUID());
    }

    public Id(UUID uuid) {
        this.uuid = uuid.toString();
    }

    public static Id createId() {
        return new Id(UUID.randomUUID());
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return Objects.equals(uuid, id.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "UUID(" + uuid + ")";
    }
}
