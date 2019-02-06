package isswatcher.manuelweb.at.UI.Activities;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.time.LocalTime;

import androidx.core.app.ActivityCompat;
import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.Exceptions.LocationNotEnabledException;
import isswatcher.manuelweb.at.Services.Infrastructure.DateManipulation;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.IssPassResponse;
import isswatcher.manuelweb.at.Services.Models.IssPasses;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CurrentLocationActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private TextView currentPosition;
    private LinearLayout passesWrap;
    protected LocationManager locationManager;
    private FusedLocationProviderClient client;
    public boolean allPermissionsGranted = false;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_current_location);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        currentPosition = findViewById(R.id.currentPosition);
        passesWrap = findViewById(R.id.passesWrap);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.back_button).setOnTouchListener(mDelayHideTouchListener);
        currentPosition.setPaintFlags(currentPosition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public void goBack(View v) {
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        client = LocationServices.getFusedLocationProviderClient(CurrentLocationActivity.this);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
        LocationUpdate locationUpdate = new LocationUpdate(this);
        locationUpdate.start();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(CurrentLocationActivity.this, new String[]{ACCESS_FINE_LOCATION},1);
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = true;
                } else {
                    String errorMessage = "Permissions denied by the user.";
                    Log.e("permissions", errorMessage);
                    Toast.makeText(CurrentLocationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    CurrentLocationActivity.this.finish();
                }
                return;
            }
        }
    }

    public class LocationUpdate extends Thread {
        CurrentLocationActivity mainActivity;
        public LatLng position;
        public boolean errorOccured = false;
        public String errorMessage = "";

        public LocationUpdate(CurrentLocationActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        public void run() {
            try {
                requestLocationPermission();
                updateLocation();
                updateUiInfo();

            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "The following error occured: " + e.getMessage();

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("internet", errorMessage);
                        Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

                errorOccured = true;
            } catch (LocationNotEnabledException e) {
                e.printStackTrace();
                showAlertToTurnOnLocation();
                //no errorOccured=true; because else the activity would be closed before the dialog was shown
                errorMessage = "Location service is not enabled";

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("locationService", errorMessage);
                        Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
            if (errorOccured) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.finish();
                    }
                });
            }
        }

        public void requestLocationPermission()
        {
            requestPermission();
            while (!allPermissionsGranted)
            {
                //wait for permission request to finish
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void updateLocation() throws LocationNotEnabledException {
            //getLocalNetworkLocation();

            position = new LatLng(35.711212, -95.995934);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentPosition.setText(position.latitude + " / " + position.longitude);
                }
            });
        }

        public void updateUiInfo() throws IOException {
            final IssPasses issPasses = IssLiveData.GetNextFiveTimeIssPasses(position.latitude, position.longitude);
            final String timesToPassText = issPasses.getResponse().size() + " next times the ISS will pass:";

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    passesWrap.removeAllViews();

                    //main wrap
                    LinearLayout placeHolderView = new LinearLayout(mainActivity);
                    placeHolderView.setOrientation(LinearLayout.VERTICAL);

                    //title
                    TextView issWillPassNextTimeTextPlaceholder = new TextView(mainActivity);
                    issWillPassNextTimeTextPlaceholder.setTextSize(28);
                    issWillPassNextTimeTextPlaceholder.setGravity(Gravity.CENTER);
                    issWillPassNextTimeTextPlaceholder.setText(timesToPassText);

                    //add title to wrap
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 0, 0, 24);
                    LinearLayout titleWarp = new LinearLayout(mainActivity);
                    titleWarp.setOrientation(LinearLayout.VERTICAL);
                    titleWarp.addView(issWillPassNextTimeTextPlaceholder, layoutParams);
                    placeHolderView.addView(titleWarp);

                    for (IssPassResponse pass : issPasses.getResponse()) {
                        //Pass date
                        TextView issPassDate = new TextView(mainActivity);
                        issPassDate.setTextSize(24);
                        issPassDate.setText(DateManipulation.getDateByUnixTimestamp(pass.getRisetime(), "dd.MM.yyyy - HH:mm:ss"));

                        //Pass time
                        LinearLayout issPassTimeWrap = new LinearLayout(mainActivity);
                        TextView issPassTimePlaceholder = new TextView(mainActivity);
                        issPassTimePlaceholder.setText("Duration: ");
                        issPassTimePlaceholder.setTextSize(20);
                        issPassTimeWrap.addView(issPassTimePlaceholder);
                        TextView issPassTime = new TextView(mainActivity);
                        issPassTime.setPaintFlags(currentPosition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        LocalTime timeOfDay = LocalTime.ofSecondOfDay(pass.getDuration());
                        issPassTime.setText(timeOfDay.toString() + "h");
                        issPassTime.setTextSize(20);
                        issPassTimeWrap.addView(issPassTime);

                        //Seperator
                        LinearLayout.LayoutParams layoutParamsForSeperator = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                        layoutParamsForSeperator.setMargins(0, 24, 0, 0);
                        View seperator = new View(mainActivity);
                        //seperator.setMinimumHeight(0);
                        seperator.setBackgroundColor(Color.GRAY);

                        //Wrap
                        LinearLayout.LayoutParams layoutParamsForPass = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParamsForPass.setMargins(0, 80, 0, 0);
                        LinearLayout singlePassWrap = new LinearLayout(mainActivity);
                        singlePassWrap.setOrientation(LinearLayout.VERTICAL);
                        singlePassWrap.addView(issPassDate, layoutParamsForPass);
                        singlePassWrap.addView(issPassTimeWrap);
                        singlePassWrap.addView(seperator, layoutParamsForSeperator);

                        placeHolderView.addView(singlePassWrap);
                    }

                    passesWrap.addView(placeHolderView);
                }
            });
        }

        public void getLocalNetworkLocation() throws LocationNotEnabledException {
            if (!isLocationEnabled()) {
                throw new LocationNotEnabledException("Location Service is not enabled/provided for this app.");
            }

            if (ActivityCompat.checkSelfPermission(CurrentLocationActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            client.getLastLocation().addOnSuccessListener(CurrentLocationActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null)
                    {
                        position = new LatLng(location.getLatitude(), location.getLatitude());
                    }
                }
            });

        }

        private void showAlertToTurnOnLocation() {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    final AlertDialog dialog = builder
                            .setTitle("Enable Location")
                            .setMessage("Your Locations Settings are turned off.\nPlease enable your location.")
                            .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(myIntent);
                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainActivity.finish();
                                        }
                                    });

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainActivity.finish();
                                        }
                                    });

                                }
                            }).create();

                    dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0,0,24,0);

                            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                            negativeButton.setTextColor(Color.BLACK);
                            positiveButton.setTextColor(Color.BLACK);
                            negativeButton.setLayoutParams(params);
                        }
                    });

                    dialog.show();
                }
            });
        }
    }

}
