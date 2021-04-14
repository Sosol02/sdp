package com.github.onedirection.geolocation;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.onedirection.utils.HTTP;
import com.github.onedirection.utils.Monads;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** OpenStreetMap's geocoding service. */
public final class NominatimGeocoding implements GeocodingService {
    private static final String SEARCH_QUERY_FORMAT = "https://nominatim.openstreetmap.org/search?limit=%s&format=json&q=%s";
    private static final String REVERSE_QUERY_FORMAT = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s";

    private static final String NAME_FIELD = "display_name";
    private static final String LAT_FIELD = "lat";
    private static final String LON_FIELD = "lon";

    private static final String LOGCAT_TAG = "NominatimGeocoding";

    private final RequestQueue requestQueue;

    public static final int MAX_RESULTS = 50;

    public NominatimGeocoding(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
    }

    public NominatimGeocoding(Context ctx){
        this(Volley.newRequestQueue(ctx));
    }

    private static String generateSearchRequestURL(String query, int countLimit){
        return String.format(SEARCH_QUERY_FORMAT, countLimit, HTTP.encode(query));
    }

    private static String generateReverseRequestURL(Coordinates coordinates){
        return String.format(REVERSE_QUERY_FORMAT, coordinates.latitude, coordinates.longitude);
    }

    private static Optional<NamedCoordinates> parseResult(JSONObject json){
        Log.d(LOGCAT_TAG, "Parsing location: " + json);
        try {
            return Optional.of(new NamedCoordinates(json.getDouble(LAT_FIELD), json.getDouble(LON_FIELD), json.getString(NAME_FIELD)));
        }
        catch(Exception e){
            Log.d(LOGCAT_TAG, "Parsing failed");
            return Optional.empty();
        }
    }

    private static Optional<NamedCoordinates> parseResult(JSONArray json, int idx){
        try {
            return parseResult(json.getJSONObject(idx));
        }
        catch(Exception e){
            Log.d(LOGCAT_TAG, "Parsing failed");
            return Optional.empty();
        }
    }

    private static List<NamedCoordinates> parseResult(JSONArray json){
        List<NamedCoordinates> result = new ArrayList<>();
        for(int i = 0; i < json.length(); ++i){
            try {
                result.add(parseResult(json.getJSONObject(i)).get());
            }
            catch(Exception ignored){}
        }
        return result;
    }

    private CompletableFuture<JSONArray> sendArrayRequest(String url){
        CompletableFuture<JSONArray> result = new CompletableFuture<>();

        Log.d(LOGCAT_TAG, "Request: " + url);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                result::complete,
                result::completeExceptionally
        );
        requestQueue.add(request);
        return result;
    }

    private CompletableFuture<JSONObject> sendObjectRequest(String url){
        CompletableFuture<JSONObject> result = new CompletableFuture<>();

        Log.d(LOGCAT_TAG, "Request: " + url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                result::complete,
                result::completeExceptionally
        );
        requestQueue.add(request);
        return result;
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(String locationName) {
        return Monads.flatten(
                sendArrayRequest(generateSearchRequestURL(locationName, 1)).thenApply(r -> parseResult(r, 0))
        );
    }

    @Override
    public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
        if(count > MAX_RESULTS){
            Log.w(LOGCAT_TAG, "The specified number of results is over Nominatim's limit (which is 50).");
        }
        else if(count < 1){
            throw new IllegalArgumentException("Count cannot be below 1.");
        }

        return sendArrayRequest(generateSearchRequestURL(locationName, count)).thenApply(NominatimGeocoding::parseResult);
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
        return Monads.flatten(
                sendObjectRequest(generateReverseRequestURL(coordinates)).thenApply(NominatimGeocoding::parseResult)
        );
    }
}
