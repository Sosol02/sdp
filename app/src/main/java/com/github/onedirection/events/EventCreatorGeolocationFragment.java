package com.github.onedirection.events;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.onedirection.R;

public class EventCreatorGeolocationFragment extends Fragment {

    private EventCreatorViewModel mViewModel;

    public static EventCreatorGeolocationFragment newInstance() {
        return new EventCreatorGeolocationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_creator_geolocation_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EventCreatorViewModel.class);
        // TODO: Use the ViewModel
    }

}