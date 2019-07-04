//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.15
//
package com.smidur.aventon;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.AWSMobileClient;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

/**
 * Application class responsible for initializing singletons and other common components.
 */
public class AventonApplication extends Application {
    private static final String LOG_TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Application.onCreate - Initializing application...");
        super.onCreate();
        initializeApplication();
        Log.d(LOG_TAG, "Application.onCreate - Application initialized OK");

        initCrashlytics();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    private void initializeApplication() {
        AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());

        // ...Put any application-specific initialization logic here...
    }
    private void initCrashlytics() {
        Crashlytics crashlyticsKit = new com.crashlytics.android.Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(false).build())
                .build();
        Fabric.with(this, crashlyticsKit, new Crashlytics());
    }
}
