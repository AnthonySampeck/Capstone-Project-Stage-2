package com.example.newonnetflix.ui;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;


public class AnalyticsApplication extends Application {
    private Tracker mTracker;


    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            mTracker = analytics.newTracker("UA-72860540-1");
        }
        return mTracker;
    }
}