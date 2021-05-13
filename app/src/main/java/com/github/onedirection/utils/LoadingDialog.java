package com.github.onedirection.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.github.onedirection.R;

public class LoadingDialog {
    private final Context context;
    private AlertDialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    @SuppressLint("InflateParams")
    public void startLoadingAnimation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}
