package com.github.onedirection.database;

import java.util.Objects;
import java.util.UUID;

public class Id {
    // these fields are what's compared for equality
    // the UUID are extremely unique (2^122 possibilities)
    // and the time thing uniques it
    // It would need the same uuid to be generated in the same millis
    // for a collision to happen.
    private final UUID uuid;
    private final long creationTime;

    private Id(UUID uuid, long creationTime) {
        this.uuid = uuid;
        this.creationTime = creationTime;
    }

    public static Id createId() {
        return new Id(UUID.randomUUID(), System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return creationTime == id.creationTime &&
                uuid.equals(id.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, creationTime);
    }
}
