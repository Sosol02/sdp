package com.github.onedirection.navigation.fragment.map;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

public class MarkerSymbolManager {

    private final SymbolManager symbolManager;
    private final List<Symbol> markers;

    public MarkerSymbolManager(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
        this.markers = new ArrayList<>();
        symbolManager.setIconAllowOverlap(true);
    }

    public Symbol addMarker(LatLng position) {
        Symbol marker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage(MapFragment.SYMBOL_ID)
                .withIconSize(2f)
                );
        markers.add(marker);
        return marker;
    }

    public List<Symbol> addMarkers(LatLng... positions) {
        List<Symbol> markers = new ArrayList<>();
        for (LatLng position: positions) {
            markers.add(addMarker(position));
        }
        return markers;
    }

    public void removeMarker (Symbol marker) {
        symbolManager.delete(marker);
        markers.remove(marker);
    }

    public void removeAllMarker () {
        symbolManager.deleteAll();
        markers.clear();
    }

    public List<Symbol> getAllMarkers() {
        return markers;
    }
}
