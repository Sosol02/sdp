package com.github.onedirection.utils;

public final class ObserverPattern {
    private ObserverPattern() {}

    @FunctionalInterface
    public interface Observer<T> {
        void onObservableUpdate(Observable<T> subject, T value);
    }

    public interface Observable<T> {
        boolean addObserver(Observer<T> observer);
        boolean removeObserver(Observer<T> observer);
    }
}
