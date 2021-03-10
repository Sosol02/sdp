package com.github.onedirection;

import com.github.onedirection.database.Id;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class IdUnitTest {
    private static int testNum = 500;

    private ArrayList<Id> genManyIds(int count) {
        ArrayList<Id> ids = new ArrayList<Id>();
        // generate ids
        for (int i = 0; i < count; ++i) {
            ids.add(Id.createId());
        }
        return ids;
    }

    @Test
    public void differentIdsCompareCorrectly() {
        ArrayList<Id> ids = genManyIds(testNum);
        // test all combinations
        for (int i = 0; i < ids.size(); ++i) {
            for (int j = i + 1; j < ids.size(); ++j) {
                assertNotEquals(ids.get(i), ids.get(j));
            }
        }
    }

    @Test
    public void sameIdsCompareCorrectly() {
        ArrayList<Id> ids = genManyIds(testNum);
        for (Id id : ids) {
            // Id's only job is to compare correctly, so we check
            assertEquals(id, id);
        }
    }
}
