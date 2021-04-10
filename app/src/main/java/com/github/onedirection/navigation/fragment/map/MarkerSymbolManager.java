package com.github.onedirection.navigation.fragment.map;

import android.util.Log;

import androidx.annotation.NonNull;

import com.github.onedirection.events.Event;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NominatimGeocoding;
import com.github.onedirection.utils.Pair;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MarkerSymbolManager {

    public static final String LOG_TAG = "MarkerSymbolManager";

    private final SymbolManager symbolManager;
    private final List<Symbol> markers;
    private final Map<Symbol, Event> eventMap = new HashMap<>();
    private final MapFragment fragment;

    public MarkerSymbolManager(SymbolManager symbolManager, MapFragment fragment) {
        Objects.requireNonNull(fragment);
        this.symbolManager = symbolManager;
        this.markers = new ArrayList<>();
        this.fragment = fragment;
        symbolManager.setIconAllowOverlap(true);
        symbolManager.addClickListener(symbol -> {
            Log.d(LOG_TAG, "Symbol clicked: " + symbol);
            Event event = eventMap.get(symbol);
            assert event != null;
            fragment.setBottomSheetEvent(event);
            fragment.showBottomSheet();
        });
    }

    public Symbol addMarker(LatLng position) {
        Log.d(LOG_TAG, "Adding marker at LatLng: "  + position.toString());
        Symbol marker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage(MapFragment.SYMBOL_ID)
                .withIconSize(2f)
                );
        Log.d(LOG_TAG, "Finished adding marker at LatLng: "  + position.toString());
        markers.add(marker);
        return marker;
    }

    public List<Symbol> addMarkers(LatLng... positions) {
        List<Symbol> markers = new ArrayList<>();
        for (LatLng position : positions) {
            markers.add(addMarker(position));
        }
        return markers;
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
            NominatimGeocoding geocoding = new NominatimGeocoding(fragment.getContext());
            geocoding.getBestNamedCoordinates(event.getLocationName())
                    .thenApply(namedCoordinates -> {
                        LatLng latLng = new LatLng(namedCoordinates.latitude, namedCoordinates.longitude);
                        fragment.getActivity().runOnUiThread(() -> future.complete(new Pair<>(addEventMarkerAt(event, latLng), latLng)));
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

    public List<Symbol> getAllMarkers() {
        return Collections.unmodifiableList(markers);
    }

    public Map<Symbol, Event> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }
}
