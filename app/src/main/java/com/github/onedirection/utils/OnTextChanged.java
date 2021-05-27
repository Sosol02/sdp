package com.github.onedirection.utils;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Consumer;

/**
 * Class representing a text observer
 */
public final class OnTextChanged implements TextWatcher {

    private final Consumer<String> callback;

    public static TextWatcher onTextChanged(Consumer<String> callback){
        return new OnTextChanged(callback);
    }

    public OnTextChanged(Consumer<String> callback) {
        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        callback.accept(s.toString());
    }
}
