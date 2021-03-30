package com.github.onedirection.navigation.fragment.map;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

public class MapSymbolManager {

    private final SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();

    public MapSymbolManager(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
    }

    public void addMarker(LatLng position) {
        Symbol marker = symbolManager.create(new SymbolOptions()
                .withLatLng(position)
                .withIconImage("marker_map")
                .withIconOffset(new Float[] {0.0f, -10.5f})
                );
        symbols.add(marker);
    }

    public void updateSymbolsOffset(double cameraZoom) {
        for (Symbol symbol: symbols) {
            symbol.setIconOffset(new PointF(0.0f, -10.f));
        }
    }
}
