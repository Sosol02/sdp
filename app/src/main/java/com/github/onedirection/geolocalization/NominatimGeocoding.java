package com.github.onedirection.geolocalization;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.onedirection.utils.HTTP;
import com.github.onedirection.utils.Monads;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** OpenStreetMap's geocoding service. */
public final class NominatimGeocoding implements GeocodingService {
    private static final String QUERY_FORMAT = "https://nominatim.openstreetmap.org/search?format=json&q=%s";
    private static final String NAME_FIELD = "display_name";
    private static final String LAT_FIELD = "lat";
    private static final String LON_FIELD = "lon";

    private static final String LOGCAT_TAG = "NominatimGeocoding";

    private final RequestQueue requestQueue;

    public NominatimGeocoding(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
    }

    public NominatimGeocoding(Context ctx){
        this(Volley.newRequestQueue(ctx));
    }

    private static String generateRequestURL(String query){
        return String.format(QUERY_FORMAT, HTTP.encode(query));
    }

    private static Optional<NamedCoordinates> parseResult(JSONArray json){
        Log.d(LOGCAT_TAG, "Parsing: " + json);
        try {
            JSONObject best = json.getJSONObject(0);
            return Optional.of(new NamedCoordinates(best.getDouble(LAT_FIELD), best.getDouble(LON_FIELD), best.getString(NAME_FIELD)));
        }
        catch(Exception e){
            Log.d(LOGCAT_TAG, "Parsing failed");
            return Optional.empty();
        }
    }

    private CompletableFuture<JSONArray> sendRequest(String query){
        CompletableFuture<JSONArray> result = new CompletableFuture<>();
        String url = generateRequestURL(query);

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

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(String locationName) {
        return Monads.flatten(sendRequest(locationName).thenApply(NominatimGeocoding::parseResult));
    }
}
