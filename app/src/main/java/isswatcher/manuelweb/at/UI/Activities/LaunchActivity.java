package isswatcher.manuelweb.at.UI.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.IssLocation;
import isswatcher.manuelweb.at.Services.Models.IssPeople;
import isswatcher.manuelweb.at.Services.Models.Person;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LaunchActivity extends AppCompatActivity {

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
    private TextView currentPeople;
    private LinearLayout addObservationWrap;
    private LinearLayout astronautsList;
    ImageView mapsIcon;
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
            mControlsView.setVisibility(View.VISIBLE);
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
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.activity_launch);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        currentPosition = findViewById(R.id.currentPosition);
        currentPeople = findViewById(R.id.currentPeople);
        mapsIcon = (ImageView) findViewById(R.id.mapsImage);
        addObservationWrap = findViewById(R.id.add_observation_wrap);
        astronautsList = findViewById(R.id.listOfAstronauts);

        currentPosition.setPaintFlags(currentPosition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        currentPeople.setPaintFlags(currentPosition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        currentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(v);
            }
        });
        addObservationWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addObservation(v);
            }
        });
        mapsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(mapsIcon);
            }
        });
        findViewById(R.id.exit_button).setOnTouchListener(mDelayHideTouchListener);
    }

    public void openMap(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void openCurrentLocation(View v) {
        Intent intent = new Intent(this, CurrentLocationActivity.class);
        startActivity(intent);
    }

    public void viewObservations(View v) {
        Intent intent = new Intent(this, ObservationsActivity.class);
        startActivity(intent);
    }

    public void addObservation(View v) {
        Intent intent = new Intent(this, AddEditObservationActivity.class);
        startActivity(intent);
    }

    public void closeApp(View v) {
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        InfoUpdate infoUpdate = new InfoUpdate(this);
        infoUpdate.start();
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


    public class InfoUpdate extends Thread {
        LaunchActivity mainActivity;
        public LatLng position;
        public boolean errorOccured = false;

        public InfoUpdate(LaunchActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        public void run() {

            while(true)
            {
                try {
                    UpdateCityName();
                    UpdateAstronauts();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("iss_internet", "The following error occured: "+e.getMessage());

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String errorMessage = "Could not get Internet Connection";
                            Log.e("iss", errorMessage);

                            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(mainActivity);
                            dlgAlert.setMessage(errorMessage);
                            dlgAlert.setTitle("Error");
                            dlgAlert.setCancelable(true);
                            dlgAlert.create().show();
                        }
                    });

                    errorOccured = true;
                }
                if(errorOccured)
                {
                    break;
                }

                sleep();
            }
        }

        public void sleep()
        {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void UpdateCityName() throws IOException {
                IssLocation currentLocation = IssLiveData.GetIssLocation();
                Geocoder gcd = new Geocoder(mainActivity, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(
                        currentLocation.getIssPosition().getLatitude(),
                        currentLocation.getIssPosition().getLongitude(),
                        1);

                final String newCityName = addresses.size() > 0 ? addresses.get(0).getCountryName() : "Unknown";

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentPosition.setText(newCityName);
                    }
                });
        }

        public void UpdateAstronauts() throws IOException {
            final IssPeople currentPeopleOnIss = IssLiveData.GetPeopleOnIss();

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Update astronauts count
                    currentPeople.setText(Integer.toString(currentPeopleOnIss.getPeople().size()));
                    astronautsList.removeAllViews();
                    LinearLayout placeHolderView = new LinearLayout(mainActivity);
                    placeHolderView.setOrientation(LinearLayout.VERTICAL);

                    //Update astronauts list
                    for(Person person : currentPeopleOnIss.getPeople())
                    {
                        //Astronaut Name
                        TextView personName = new TextView(mainActivity);
                        personName.setTextSize(20);
                        personName.setText(person.getName());

                        //Astronaut Craft
                        LinearLayout craftWrap = new LinearLayout(mainActivity);
                        TextView craftTextPlaceholder = new TextView(mainActivity);
                        craftTextPlaceholder.setText("Craft: ");
                        craftWrap.addView(craftTextPlaceholder);
                        TextView craft = new TextView(mainActivity);
                        craft.setPaintFlags(currentPosition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        craft.setText(person.getCraft());
                        craftWrap.addView(craft);

                        //Wrap
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 12, 0, 0);
                        LinearLayout personWarp = new LinearLayout(mainActivity);
                        personWarp.setOrientation(LinearLayout.VERTICAL);
                        personWarp.addView(personName, layoutParams);
                        personWarp.addView(craftWrap);

                        placeHolderView.addView(personWarp);
                    }

                    astronautsList.addView(placeHolderView);
                }
            });
        }
    }
}
