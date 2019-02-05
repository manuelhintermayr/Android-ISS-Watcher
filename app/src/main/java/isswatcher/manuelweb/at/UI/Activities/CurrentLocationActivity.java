package isswatcher.manuelweb.at.UI.Activities;

import android.annotation.SuppressLint;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.LocalTime;

import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.Exceptions.LocationNotEnabledException;
import isswatcher.manuelweb.at.Services.Infrastructure.DateManipulation;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.IssPassResponse;
import isswatcher.manuelweb.at.Services.Models.IssPasses;

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

    public void goBack(View v)
    {
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

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

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

                updateLocation();
                updateUiInfo();

            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "The following error occured: "+e.getMessage();
                Log.e("internet", errorMessage);

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

                //errorMessage
            }
            if(errorOccured)
            {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.finish();
                    }
                });
            }
        }

        public void updateLocation() throws LocationNotEnabledException {
            getLocalNetworkLocation();

            position = new LatLng(35.711212, -95.995934);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentPosition.setText(position.latitude+" / "+position.longitude);
                }
            });
        }

        public void updateUiInfo() throws IOException {
            final IssPasses issPasses = IssLiveData.GetNextFiveTimeIssPasses(position.latitude, position.longitude);
            final String timesToPassText = issPasses.getResponse().size()+" next times the ISS will pass:";

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
                    layoutParams.setMargins(0,  0, 0, 24);
                    LinearLayout titleWarp = new LinearLayout(mainActivity);
                    titleWarp.setOrientation(LinearLayout.VERTICAL);
                    titleWarp.addView(issWillPassNextTimeTextPlaceholder, layoutParams);
                    placeHolderView.addView(titleWarp);

                    for(IssPassResponse pass : issPasses.getResponse())
                    {
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
                        issPassTime.setText(timeOfDay.toString()+"h");
                        issPassTime.setTextSize(20);
                        issPassTimeWrap.addView(issPassTime);

                        //Seperator
                        LinearLayout.LayoutParams layoutParamsForSeperator = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                        layoutParamsForSeperator.setMargins(0,24,0,0);
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
            if(!isLocationEnabled())
            {
                throw new LocationNotEnabledException("Location Service is not enabled/provided for this app.");
            }
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
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
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
