package com.github.onedirection.navigation.fragment.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Calendar view model in the navigation fragment hierarchy
 */
public class CalendarViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CalendarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is an example text for Calendar");
    }

    public LiveData<String> getText() {
        return mText;
    }
}