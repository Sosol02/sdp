package com.github.onedirection.navigation.fragment.map;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private MarkerSymbolManager markerSymbolManager;
    private final String SYMBOL_ID = "MARKER_MAP";
    private final String BUNDLE_ID = "bundleT";
    private final String MARKERS_ID = "markers_size";
    private Bundle savedState = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(BUNDLE_ID);
        }
        List<LatLng> previousMarkers = new ArrayList<>();
        if (savedState != null) {
            int sizeMarkers = savedState.getInt(MARKERS_ID);
            for (int key = 0; key < sizeMarkers; ++key) {
                previousMarkers.add(savedInstanceState.getParcelable(String.valueOf(key)));
            }
        }
        savedState = null;

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                initializeMarkerSymbolManager(style);
            });

            for (LatLng previousMarker: previousMarkers) {
                markerSymbolManager.addMarker(previousMarker);
            }

            mapboxMap.addOnMapClickListener(point -> {
                markerSymbolManager.removeAllMarker();
                markerSymbolManager.addMarker(point);
                return false;
            });
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putBundle(BUNDLE_ID, savedState != null ? savedState : saveMapState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            savedState = savedInstanceState.getBundle(BUNDLE_ID);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveMapState();
    }



    private Bundle saveMapState() {
        Bundle saveState = new Bundle();
        if (markerSymbolManager != null) {
            List<Symbol> markers = markerSymbolManager.getAllMarkers();
            saveState.putInt(MARKERS_ID, markers.size());
            int key = 0;
            for (Symbol marker: markers) {
                saveState.putParcelable(String.valueOf(key), marker.getLatLng());
                ++key;
            }
        }
        return saveState;
    }

    private void initializeMarkerSymbolManager(@NonNull Style styleOnLoaded) {
        Drawable marker = ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_map);
        styleOnLoaded.addImage(SYMBOL_ID, BitmapUtils.getBitmapFromDrawable(marker));
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, styleOnLoaded);
        markerSymbolManager = new MarkerSymbolManager(symbolManager, SYMBOL_ID);
    }
}