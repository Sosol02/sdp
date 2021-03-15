package com.github.onedirection.testdatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.store.Id;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class ConcreteDatabaseTest {

    private ArrayList<Item> makeItems(int count) {
        ArrayList<Item> l = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            l.add(new Item(i, Integer.toString(i)));
        }
        return l;
    }

    @Test
    public void canStoreAndRetrieveItems() throws ExecutionException, InterruptedException {
        List<Item> l = makeItems(10);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        ArrayList<Id> ids = new ArrayList<>();
        for (int i = 0; i < l.size(); ++i) {
            ids.add(db.store(l.get(i)).get());

        }
        for (int i = 0; i < l.size(); ++i) {
            Item item = db.retrieve(ids.get(i), ItemStorer.getInstance()).get();
            assertEquals(item, l.get(i));
        }
    }
}
