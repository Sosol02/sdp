package com.github.onedirection.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;

public class CacheTest {

    private Map<Integer, String> makeMap() {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < 20; ++i) {
            map.put(i, Integer.toString(i));
        }
        return map;
    }

    @Test
    public void cacheReturnsCorrectValues() {
        Cache<Integer, String> cache = new Cache<>((x) -> Integer.toString(x), 5);
        for (int i = 0; i < 10; ++i) {
            assertThat(cache.get(i), is(Integer.toString(i)));
        }

        for (int i = 0; i < cache.getMaxHistory(); ++i) {
            for (int j = 0; j < cache.getMaxHistory(); ++j) {
                assertThat(cache.get(j), is(Integer.toString(j)));
            }
        }
    }

    @Test
    public void cacheRespectsCapacity() {
        final int[] counter = {0};
        Cache<Integer, String> cache = new Cache<>((x) -> {
            counter[0] += 1;
            return Integer.toString(x);
        }, 5);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < cache.getMaxHistory(); ++j) {
                assertThat(cache.get(j), is(Integer.toString(j)));
            }
        }

        assertThat(counter[0], is(cache.getMaxHistory()));
    }

    @Test
    public void cacheDropsOldElements() {
        Cache<Integer, String> cache = new Cache<>((x) -> Integer.toString(x), 2);
        cache.get(0);
        cache.get(1);
        Map<Integer, String> map1 = cache.getMap();

        Map<Integer, String> map1correct = new HashMap<>();
        map1correct.put(0, "0");
        map1correct.put(1, "1");

        assertThat(map1, is(map1correct));

        cache.get(2);
        Map<Integer, String> map2 = cache.getMap();

        Map<Integer, String> map2correct = new HashMap<>();
        map2correct.put(1, "1");
        map2correct.put(2, "2");

        assertThat(map2, is(map2correct));
    }

    @Test
    public void cacheUnEqualsCache() {
        Cache<Integer, String> cache1 = new Cache<>((x) -> Integer.toString(x), 5);
        Cache<Integer, String> cache2 = new Cache<>((x) -> Integer.toString(x), 5);
        assertThat(cache1.equals(cache2), is(false));
    }

    @Test
    public void cacheDefaultMaxHistoryNotZero() {
        assertThat(new Cache<>((x) -> x).getMaxHistory(), is(not(0)));
    }

}
