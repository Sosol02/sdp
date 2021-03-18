package com.github.onedirection.navigation.fragment.calendar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.onedirection.R;

public class DaySelectPopupFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.popup_title).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        }).setItems(R.array.day_select_actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //add an event
                    case 0:
                        break;
                    //view events
                    case 1:
                        break;
                    default:
                        throw new IllegalArgumentException("Button index out of bounds");
                }
            }
        });
        return builder.create();

    }
}