package com.github.onedirection.geolocation.geocoding;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.HTTP;
import com.github.onedirection.utils.Monads;
import com.github.onedirection.utils.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * OpenStreetMap's geocoding service.
 */
public final class NominatimGeocoding implements GeocodingService {
    private static final String SEARCH_QUERY_FORMAT = "https://nominatim.openstreetmap.org/search?limit=%s&format=json&q=%s";
    private static final String REVERSE_QUERY_FORMAT = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s";

    private static final String NAME_FIELD = "display_name";
    private static final String LAT_FIELD = "lat";
    private static final String LON_FIELD = "lon";

    private static final String LOGCAT_TAG = "NominatimGeocoding";
    private static final String USER_AGENT = "1Direction";

    public static final int MAX_RESULTS = 50;
    private static final int MIN_DELAY_MS = 1_000;

    private final RequestQueue requestQueue;
    private final Handler delayer;
    private long lastRequest;

    public NominatimGeocoding(RequestQueue requestQueue, Handler delayer) {
        this.requestQueue = requestQueue;
        this.delayer = delayer;
        this.lastRequest = 0;
    }

    public NominatimGeocoding(Context ctx) {
        this(Volley.newRequestQueue(ctx), new Handler(ctx.getMainLooper()));
    }

    private static String generateSearchRequestURL(String query, int countLimit) {
        return String.format(SEARCH_QUERY_FORMAT, countLimit, HTTP.encode(query));
    }

    private static String generateReverseRequestURL(Coordinates coordinates) {
        return String.format(REVERSE_QUERY_FORMAT, coordinates.latitude, coordinates.longitude);
    }

    private static Optional<NamedCoordinates> parseResult(JSONObject json) {
        try {
            return Optional.of(new NamedCoordinates(json.getDouble(LAT_FIELD), json.getDouble(LON_FIELD), json.getString(NAME_FIELD)));
        } catch (Exception e) {
            Log.d(LOGCAT_TAG, "Parsing failed: " + json);
            return Optional.empty();
        }
    }

    private static List<NamedCoordinates> parseResult(JSONArray json) {
        List<NamedCoordinates> result = new ArrayList<>();
        for (int i = 0; i < json.length(); ++i) {
            try {
                result.add(parseResult(json.getJSONObject(i)).get());
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private Pair<CompletableFuture<JSONArray>, JsonArrayRequest> generateArrayRequest(String url) {
        CompletableFuture<JSONArray> result = new CompletableFuture<>();

        Log.d(LOGCAT_TAG, "Request: " + url);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                result::complete,
                result::completeExceptionally
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Collections.singletonMap("User-Agent", USER_AGENT);
            }
        };
        return Pair.of(result, request);
    }

    private Pair<CompletableFuture<JSONObject>, JsonObjectRequest> generateObjectRequest(String url) {
        CompletableFuture<JSONObject> result = new CompletableFuture<>();

        Log.d(LOGCAT_TAG, "Request: " + url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                result::complete,
                result::completeExceptionally
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Collections.singletonMap("User-agent", USER_AGENT);
            }
        };
        return Pair.of(result, request);
    }

    private <T, S extends Request<T>> CompletableFuture<T> sendRequest(Pair<CompletableFuture<T>, S> valReqPair) {
        CompletableFuture<T> result = valReqPair.first;
        S request = valReqPair.second;

        result = result.whenComplete((t, throwable) -> {
            if (throwable != null) {
                request.cancel();
            }
        });


        long delay = Math.max(0, MIN_DELAY_MS - (SystemClock.uptimeMillis() - this.lastRequest));
        delayer.postDelayed(() -> requestQueue.add(request), delay);
        this.lastRequest = SystemClock.uptimeMillis() + delay;

        return result;
    }

    @Override
    public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
        if (count > MAX_RESULTS) {
            Log.w(LOGCAT_TAG, String.format("The specified number of results is over Nominatim's limit (which is %s).", MAX_RESULTS));
            count = MAX_RESULTS;
        } else if (count < 1) {
            throw new IllegalArgumentException("Count cannot be below 1.");
        }

        return sendRequest(generateArrayRequest(generateSearchRequestURL(locationName, count)))
                .thenApply(NominatimGeocoding::parseResult);
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
        return Monads.flatten(
                sendRequest(generateObjectRequest(generateReverseRequestURL(coordinates)))
                        .thenApply(NominatimGeocoding::parseResult)
        );
    }
}
