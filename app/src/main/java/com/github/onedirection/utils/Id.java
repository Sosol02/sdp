package com.github.onedirection.utils;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

/**
 * Class representing an ID to give to storable objects to uniquely identify them.
 */
@Immutable
public class Id implements Serializable {
    public final static int LENGTH = 36;
    public final static int SEP_COUNT = 4;

    // This field is what's compared for equality
    // the UUIDs are extremely unique (2^122 possibilities)
    // and the time attribute uniques it
    // It would need the same uuid to be generated in the same millis
    // for a collision to happen.
    private final String uuid;

    public static Id generateRandom() {
        return new Id();
    }

    /**
     * Generate a new random Id.
     */
    public Id(){
        this(UUID.randomUUID());
    }

    public Id(UUID uuid) {
        this.uuid = uuid.toString();

        if(this.uuid.length() != LENGTH){
            throw new IllegalArgumentException("Invalid UUID length: " + this.uuid.length());
        }
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
        return "ID(" + uuid + ")";
    }
}
