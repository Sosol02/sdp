package com.github.onedirection.database;

import com.github.onedirection.utils.Id;
import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;

import java.util.Objects;

public class Item implements Storable<Item> {
    private final Id id;
    private final int number;
    private final String s;

    // used by storer
    private Item(Id id, int number, String s) {
        this.id = id;
        this.number = number;
        this.s = s;
    }

    public Item() {
        this(null, 0, null);
    }

    public Item(int number, String s) {
        this(Id.generateRandom(), number, s);
    }

    @Override
    public Id getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getS() {
        return s;
    }

    @Override
    public Storer<Item> storer() {
        return ItemStorer.getInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return number == item.number &&
                Objects.equals(id, item.id) &&
                Objects.equals(s, item.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, s);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", number=" + number +
                ", s='" + s + '\'' +
                '}';
    }
}
