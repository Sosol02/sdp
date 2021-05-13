package com.github.onedirection.utils;


import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class AndroidCacheTest {

    private final static String CACHE_NAME = "test";

    @Test
    public void cacheCanBeStoredAndLoadedFromAndroid() {
        Context ctx = ApplicationProvider.getApplicationContext();

        Function<Integer, Integer> get = i -> i;

        Cache<Integer, Integer> ref = new Cache<>(get, 2);
        for (int i = 0; i <= 9; ++i) {
            ref.get(i);
        }


        boolean dumpSuccess = AndroidCache.dumpToAndroidCache(
                ctx,
                CACHE_NAME,
                ref,
                i -> i,
                i -> i
        );
        assertThat(dumpSuccess, is(true));

        Optional<Cache<Integer, Integer>> maybeSerialized = AndroidCache.loadFromAndroidCache(
                ctx,
                CACHE_NAME,
                get,
                (Integer i) -> i,
                (Integer i) -> i
        );

        assertThat(maybeSerialized.isPresent(), is(true));

        Cache<Integer, Integer> serialized = maybeSerialized.get();

        for (Integer k : ref.getMap().keySet()) {
            assertThat(serialized.isCached(k), is(true));
        }

        BiConsumer<Integer, Integer> bothRequest = (i, old) -> {
            assertThat(ref.isCached(i), is(false));
            assertThat(serialized.isCached(i), is(false));
            ref.get(i);
            serialized.get(i);
            assertThat(ref.isCached(i), is(true));
            assertThat(serialized.isCached(i), is(true));
        };

        Consumer<Integer> noneRequest = i -> {
            assertThat(ref.isCached(i), is(true));
            assertThat(serialized.isCached(i), is(true));
        };

        bothRequest.accept(7, 8);
        noneRequest.accept(9);
        noneRequest.accept(7);
        noneRequest.accept(9);
        bothRequest.accept(0, 9);
        bothRequest.accept(9, 7);
    }

}

