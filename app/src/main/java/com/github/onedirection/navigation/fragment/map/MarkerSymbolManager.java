package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.onedirection.R;

import com.github.onedirection.database.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.geocoding.GeocodingService;
import com.github.onedirection.utils.Monads;
import com.github.onedirection.utils.Pair;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * MarkerSymbolManager is used with mapbox to be able to display marker that represent events,
 * on the map in mapfragment
 */
public class MarkerSymbolManager {

    public static final String LOG_TAG = "MarkerSymbolManager";

    private final SymbolManager symbolManager;
    private final List<Symbol> markers;
    private final Map<Symbol, Event> eventMap = new HashMap<>();
    private final MapFragment fragment;

    private Symbol tripStartMarker;

    private final static String SYMBOL_ID = "MARKER_MAP";
    private final static String TRIP_ORIGIIN_ID = "MARKER_TRIP_ORIGIN";

    public MarkerSymbolManager(Context context, MapView mapView, MapboxMap mapboxMap, Style style, MapFragment fragment) {
        Objects.requireNonNull(fragment);
        Objects.requireNonNull(context);
        initializeSymbolImage(context, style);
        this.symbolManager = new SymbolManager(mapView, mapboxMap, style);
        this.markers = new ArrayList<>();
        this.fragment = fragment;
        symbolManager.setIconAllowOverlap(true);

        symbolManager.addClickListener(symbol -> {
            Log.d(LOG_TAG, "Symbol clicked: " + symbol);
            Event event = eventMap.get(symbol);
            Objects.requireNonNull(event);

            fragment.setBottomSheetEvent(event);
            fragment.showBottomSheet();
        });
    }

    public Symbol addMarker(LatLng position) {
        Log.d(LOG_TAG, "Adding marker at LatLng: "  + position.toString());
        Symbol marker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage(SYMBOL_ID)
                .withIconSize(2f)
        );
        Log.d(LOG_TAG, "Finished adding marker at LatLng: "  + position.toString());
        markers.add(marker);
        return marker;
    }

    public Symbol setTripStartMarker(LatLng position) {
        Log.d(LOG_TAG, "setTripStartMarker");
        removeTripStartMarker();
        tripStartMarker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage(TRIP_ORIGIIN_ID)
                .withIconSize(1f)
        );
        return tripStartMarker;
    }

    public void removeTripStartMarker() {
        Log.d(LOG_TAG, "removeTripStartMarker");
        if (tripStartMarker != null) {
            symbolManager.delete(tripStartMarker);
        }
    }

    public CompletableFuture<Pair<Symbol, LatLng>> addGeocodedEventMarker(@NonNull Event event) {
        Optional<Coordinates> optCoords = event.getCoordinates();
        if (optCoords.isPresent()) {
            Coordinates coords = optCoords.get();
            LatLng latLng = new LatLng(coords.latitude, coords.longitude);
            CompletableFuture<Pair<Symbol, LatLng>> future = new CompletableFuture<>();
            future.complete(new Pair<>(addEventMarkerAt(event, latLng), latLng));
            return future;
        } else {
            CompletableFuture<Pair<Symbol, LatLng>> future = new CompletableFuture<>();
            GeocodingService geocoding = GeocodingService.getDefaultInstance();
            geocoding.getBestNamedCoordinates(event.getLocationName())
                    .thenApply(namedCoordinates -> {
                        LatLng latLng = new LatLng(namedCoordinates.latitude, namedCoordinates.longitude);
                        fragment.requireActivity().runOnUiThread(() -> future.complete(new Pair<>(addEventMarkerAt(event, latLng), latLng)));
                        return null;
                    }).exceptionally(future::completeExceptionally);

            return future;
        }
    }

    /**
     * Add an event to the map, using its coordinates to display a marker.
     * Clicking on the marker reveals its details on the bottomSheet.
     * @param event The event to add.
     * @return The symbol associated to the added event.
     * @throws IllegalArgumentException if the symbol has no coordinates.
     */
    public Symbol addEventMarker(@NonNull Event event) {
        Optional<Coordinates> optCoords = event.getCoordinates();
        if (!optCoords.isPresent())
            throw new IllegalArgumentException("The event has no coordinates: " + event.toString());
        Coordinates coords = optCoords.get();
        LatLng latLng = new LatLng(coords.latitude, coords.longitude);
        return addEventMarkerAt(event, latLng);
    }

    public Symbol addEventMarkerAt(@NonNull Event event, @NonNull LatLng position) {
        Log.d(LOG_TAG, "addEventMarkerAt: " + position.toString() + " |" + event.toString());
        Symbol symbol = addMarker(position);
        eventMap.put(symbol, event);
        return symbol;
    }

    public void removeMarker(Symbol marker) {
        symbolManager.delete(marker);
        markers.remove(marker);
        eventMap.remove(marker);
    }

    public void removeAllMarkers() {
        symbolManager.deleteAll();
        markers.clear();
        eventMap.clear();
    }

    private void initializeSymbolImage(Context context, Style style) {
        Drawable marker = ContextCompat.getDrawable(context, R.drawable.ic_marker_map);
        style.addImage(SYMBOL_ID, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(marker)));
        Drawable markerTripOrigin = ContextCompat.getDrawable(context, R.drawable.ic_baseline_trip_origin_24);
        style.addImage(TRIP_ORIGIIN_ID, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(markerTripOrigin)));
    }

    public List<Symbol> getAllMarkers() {
        return Collections.unmodifiableList(markers);
    }

    public Map<Symbol, Event> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    public CompletableFuture<Void> syncEventsWithDb() {
        Log.d(LOG_TAG, "syncEventsWithDb");
        Database db = Database.getDefaultInstance();
        CompletableFuture<List<Event>> futEvents = db.retrieveAll(EventStorer.getInstance());
        CompletableFuture<CompletableFuture<Void>> futAddEvents = futEvents.handle((ls, err) -> {
            if (err != null) {
                Log.d(LOG_TAG, "syncEventsWithDb: error: " + err);
                // @Reviewers: should i put a counter or something to prevent infinite loop?
                return syncEventsWithDb(); // retry, hopefully it'll work some day.
            } else {
                Log.d(LOG_TAG, "syncEventsWithDb: register db events: " + ls);
                removeAllMarkers();
                ArrayList<CompletableFuture<Pair<Symbol, LatLng>>> futures = new ArrayList<>();
                for (Event e : ls) {
                    futures.add(addGeocodedEventMarker(e));
                }
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            }
        });
        return Monads.flattenFuture(futAddEvents);
    }
}
