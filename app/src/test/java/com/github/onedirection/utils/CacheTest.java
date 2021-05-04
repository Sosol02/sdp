package com.github.onedirection.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertThrows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class CacheTest {

    private Map<Integer, String> makeMap() {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < 20; ++i) {
            map.put(i, Integer.toString(i));
        }
        return map;
    }

    @Test
    public void defaultFunctionsThrow() {
        Cache<Integer, Integer> cache = new Cache<>();
        assertThrows(RuntimeException.class, () -> cache.get(0));
        assertThrows(RuntimeException.class, () -> cache.set(0, 0));
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
    public void cacheDefaultMaxHistoryIsCorrect() {
        assertThat(new Cache<>().getMaxHistory(), is(not(0)));
        assertThat(new Cache<>(75).getMaxHistory(), is(75));
    }

    @Test
    public void cacheSetWorks() {
        Map<Integer, String> map = makeMap();

        Cache<Integer, String> cache = new Cache<>(
                (k) -> map.get(k),
                (k, v) -> {
                    map.put(k, v);
                    return true;
                },
                10
        );

        cache.set(21, "21");
        assertThat(map.getOrDefault(21, "wrong"), is("21"));
        assertThat(map.getOrDefault(21, "wrong"), is(cache.get(21)));
    }

    @Test
    public void cacheInvalidateWorks() {
        final int[] counter = {0};
        Cache<Integer, String> cache = new Cache<>((x) -> {
            counter[0] += 1;
            return Integer.toString(x);
        }, 5);

        cache.get(0);
        assertThat(cache.getMap().getOrDefault(0, "wrong"), is("0"));
        assertThat(counter[0], is(1));

        cache.invalidate();

        cache.get(0);
        assertThat(cache.getMap().getOrDefault(0, "wrong"), is("0"));
        assertThat(counter[0], is(2));

        cache.invalidate();

        // test other overload
        counter[0] = 0;
        cache.get(0);
        cache.get(1);
        cache.get(2);

        assertThat(cache.getMap().getOrDefault(0, "wrong"), is("0"));
        assertThat(cache.getMap().getOrDefault(1, "wrong"), is("1"));
        assertThat(cache.getMap().getOrDefault(2, "wrong"), is("2"));
        assertThat(counter[0], is(3));

        cache.invalidate(1);
        cache.get(1);

        assertThat(counter[0], is(4));
    }

    @Test
    public void testCacheSetFunction() {
        final int[] counter = {0};
        final Cache<Integer, String> cache = new Cache<>(k -> Integer.toString(k), (k, v) -> {
            counter[0] += 1;
            return true;
        });

        cache.set(3, "3");
        cache.set(5, "5");
        cache.set(324, "324");
        cache.get(3);
        cache.get(6);
        cache.get(32465);

        assertThat(counter[0], is(3));
    }

    @Test
    public void testCacheNullValuesArentCached() {
        final int[] counter = {0};
        final Cache<Integer, String> cache = new Cache<>(k -> {
            counter[0] += 1;
            return null;
        });

        cache.get(3);
        assertThat(counter[0], is(1));
        cache.get(4);
        assertThat(counter[0], is(2));
        cache.get(3);
        assertThat(counter[0], is(3));
        cache.get(4);
        assertThat(counter[0], is(4));
    }

    @Test
    public void writeAllocateWorks() {
        final int[] counter = {0};
        final Cache<Integer, String> cache = new Cache<>(k -> {
            counter[0] += 1;
            return null;
        }, (k, v) -> true);

        cache.set(0, "0", false);
        assertThat(counter[0], is(0));
        cache.get(0);
        assertThat(counter[0], is(1));

        cache.invalidate();
        counter[0] = 0;

        cache.set(0, "0", true);
        assertThat(counter[0], is(0));
        cache.get(0);
        assertThat(counter[0], is(0));
    }


    ///////////////////////////////////////
    //           Persistence             //
    ///////////////////////////////////////

    @Test
    public void canBeStoredAndLoaded() {
        Function<Integer, Integer> get = i -> i;

        Cache<Integer, Integer> ref = new Cache<>(get, 2);
        for (int i = 0; i <= 9; ++i) {
            ref.get(i);
        }


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean dumpSuccess = ref.dumpToStream(output, i -> i, i -> i);
        assertThat(dumpSuccess, is(true));

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Optional<Cache<Integer, Integer>> maybeSerialized = Cache.loadFromStream(
                input,
                get,
                (Integer i) -> i,
                (Integer i) -> i
        );
        assertThat(maybeSerialized.isPresent(), is(true));

        Cache<Integer, Integer> serialized = maybeSerialized.get();

        for(Integer k: ref.getMap().keySet()){
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
