//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.15
//
package com.smidur.aventon;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;


import com.smidur.aventon.cloud.ApiGatewayController;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.model.SyncRideSummary;
import com.smidur.aventon.navigation.NavigationDrawer;
import com.smidur.aventon.utilities.GpsUtil;
import com.smidur.aventon.utilities.NotificationUtil;

import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /** Class name for log messages. */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Bundle key for saving/restoring the toolbar title. */
    private static final String BUNDLE_KEY_TOOLBAR_TITLE = "title";

    /** The identity manager used to keep track of the current user account. */
    private IdentityManager identityManager;

    /** The toolbar view control. */
    private Toolbar toolbar;

    /** Our navigation drawer class for handling navigation drawer logic. */
    private NavigationDrawer navigationDrawer;

    /** The helper class used to toggle the left navigation drawer open and closed. */
    private ActionBarDrawerToggle drawerToggle;

    /** Data to be passed between fragments. */
    private Bundle fragmentBundle;

    private Button   signOutButton;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 89103571;

    private CountDownLatch permissionLatch = new CountDownLatch(1);

    /**
     * Initializes the Toolbar for use with the activity.
     */
    private void setupToolbar(final Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set up the activity to use this toolbar. As a side effect this sets the Toolbar's title
        // to the activity's title.
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            // Some IDEs such as Android Studio complain about possible NPE without this check.
            assert getSupportActionBar() != null;

            // Restore the Toolbar's title.
            getSupportActionBar().setTitle(
                    savedInstanceState.getCharSequence(BUNDLE_KEY_TOOLBAR_TITLE));


        }
    }

    /**
     * Initializes the sign-in and sign-out buttons.
     */
    private void setupSignInButtons() {

        signOutButton = (Button) findViewById(R.id.button_signout);
        signOutButton.setOnClickListener(this);

    }

    /**
     * Initializes the navigation drawer menu to allow toggling via the toolbar or swipe from the
     * side of the screen.
     */
    private void setupNavigationMenu(final Bundle savedInstanceState) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerItems = (ListView) findViewById(R.id.nav_drawer_items);

        // Create the navigation drawer.
        navigationDrawer = new NavigationDrawer(this, toolbar, drawerLayout, drawerItems,
                R.id.main_fragment_container);

        String mode = getIntent().getStringExtra("mode");
        if(mode != null && mode.equals("passenger")) {
            navigationDrawer.addDemoFeatureToMenu(NavigationDrawer.Screen.PASSENGER_SCHEDULE_FRAGMENT);

        } else if (mode != null && mode.equals("driver")) {
            navigationDrawer.addDemoFeatureToMenu(NavigationDrawer.Screen.DRIVER_LOOK_FOR_RIDE);

        }

        if (savedInstanceState == null) {
            // Add the home fragment to be displayed initially.
            navigationDrawer.showHome(MainActivity.this,getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("confirm_ride")) {
            this.getIntent().putExtra("confirm_ride",true);
        }
        if(intent.hasExtra("reject_ride")) {
            NotificationUtil.i(this).cancelIncomingRideRequestNotification();
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new Thread() {
            public void run() {

                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                try{
                    permissionLatch.await();
                }catch(InterruptedException ie) {
                    //todo analytics
                }
                try {
                    GpsUtil.getLatLng(GpsUtil.getUserLocation(MainActivity.this));

                } catch(SecurityException se) {
                    //todo analytics
                    //todo retry or check before attempting so that we know permission is there.
                    return;

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onContinueOnCreate(savedInstanceState);
                    }
                });


            }
        }.start();


    }
    private void onContinueOnCreate(final Bundle savedInstanceState) {
        // Obtain a reference to the mobile client. It is created in the Application class,
        // but in case a custom Application class is not used, we initialize it here if necessary.
        AWSMobileClient.initializeMobileClientIfNecessary(this);

        // Obtain a reference to the mobile client. It is created in the Application class.
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        // Obtain a reference to the identity manager.
        identityManager = awsMobileClient.getIdentityManager();

        setContentView(R.layout.activity_main);

        setupToolbar(savedInstanceState);

        setupNavigationMenu(savedInstanceState);

        setupSignInButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread() {
            public void run() {
                try{
                    permissionLatch.await();
                }catch(InterruptedException ie) {
                    //todo analytics
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!AWSMobileClient.defaultMobileClient().getIdentityManager().isUserSignedIn()) {
                            // In the case that the activity is restarted by the OS after the application
                            // is killed we must redirect to the splash activity to handle the sign-in flow.
                            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            return;
                        }


                    }
                });
            }
        }.start();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here excluding the home button.

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // Save the title so it will be restored properly to match the view loaded when rotation
        // was changed or in case the activity was destroyed.
        if (toolbar != null) {
            bundle.putCharSequence(BUNDLE_KEY_TOOLBAR_TITLE, toolbar.getTitle());
        }
    }

    @Override
    public void onClick(final View view) {
        if (view == signOutButton) {
            // The user is currently signed in with a provider. Sign out of that provider.
            identityManager.signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // ... add any other button handling code here ...

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = this.getSupportFragmentManager();

        if (navigationDrawer.isDrawerOpen()) {
            navigationDrawer.closeDrawer();
            return;
        }
        moveTaskToBack(false);

//        if (fragmentManager.getBackStackEntryCount() == 0) {
//            if (fragmentManager.findFragmentByTag(HomeDemoFragment.class.getSimpleName()) == null) {
//                final Class fragmentClass = HomeDemoFragment.class;
//                // if we aren't on the home fragment, navigate home.
//                final Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());
//
//                fragmentManager
//                        .beginTransaction()
//                        .replace(R.id.main_fragment_container, fragment, fragmentClass.getSimpleName())
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .commit();
//
//                // Set the title for the fragment.
//                final ActionBar actionBar = this.getSupportActionBar();
//                if (actionBar != null) {
//                    actionBar.setTitle(getString(R.string.app_name));
//                }
//                return;
//            }
//        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        permissionLatch.countDown();

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Toast.makeText(MainActivity.this, R.string.accept_permission, Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Stores data to be passed between fragments.
     * @param fragmentBundle fragment data
     */
    public void setFragmentBundle(final Bundle fragmentBundle) {
        this.fragmentBundle = fragmentBundle;
    }

    /**
     * Gets data to be passed between fragments.
     * @return fragmentBundle fragment data
     */
    public Bundle getFragmentBundle() {
        return this.fragmentBundle;
    }
}
