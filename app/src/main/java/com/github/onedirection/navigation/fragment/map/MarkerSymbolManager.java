package com.github.onedirection.navigation.fragment.map;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerSymbolManager {

    private final String SYMBOL_ID;
    private final SymbolManager symbolManager;
    private final List<Symbol> symbols;

    public MarkerSymbolManager(SymbolManager symbolManager, String SYMBOL_ID) {
        this.symbolManager = symbolManager;
        this.SYMBOL_ID = SYMBOL_ID;
        this.symbols = new ArrayList<>();
        symbolManager.setIconAllowOverlap(true);
    }

    public Symbol addMarker(LatLng position) {
        Symbol marker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage(SYMBOL_ID)
                .withIconSize(2f)
                );
        symbols.add(marker);
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
        symbols.remove(marker);
    }

    public void removeAllMarker () {
        symbolManager.deleteAll();
        symbols.clear();
    }
}
