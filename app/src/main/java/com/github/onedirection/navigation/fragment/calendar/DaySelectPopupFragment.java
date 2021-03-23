package com.github.onedirection.navigation.fragment.calendar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.onedirection.R;

public class DaySelectPopupFragment extends DialogFragment {

    final private CharSequence[] buttonTitles = {"Add Event", "View Events"};
    private final int day;
    private final int month;
    private final int year;
    private final CalendarFragment calendar;

    public DaySelectPopupFragment(CalendarFragment calendar, int day, int month, int year) {
        this.calendar = calendar;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.popup_title).setItems(buttonTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //add events
                    case 0:
                        calendar.showEventCreationScreen(day, month, year);
                        break;
                    //view events
                    case 1:
                        break;
                    default:
                        throw new IllegalArgumentException("Button index out of bounds");
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        return builder.create();
    }
}