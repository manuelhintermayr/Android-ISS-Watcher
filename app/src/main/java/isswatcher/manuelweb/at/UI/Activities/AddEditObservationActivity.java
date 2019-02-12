package isswatcher.manuelweb.at.UI.Activities;

import android.annotation.SuppressLint;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.DAO.ObservationsDao;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.Models.IssLocation;
import isswatcher.manuelweb.at.Services.ObservationsDatabase;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AddEditObservationActivity extends AppCompatActivity {
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
    private TextInputEditText timestampTextField;
    private TextInputEditText latitudeTextField;
    private TextInputEditText longtitudeTextField;
    private TextInputEditText notesTextField;
    private boolean isUpdateScreen = false;
    private Observation currentObservation;
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

        setContentView(R.layout.activity_add_edit_observation);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        timestampTextField = findViewById(R.id.timestampTextfield);
        latitudeTextField = findViewById(R.id.latitudeTextfield);
        longtitudeTextField = findViewById(R.id.longitudeTextfield);
        notesTextField = findViewById(R.id.notesTextfield);

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
    }

    public void addOrUpdateEntry(View v)
    {
        final long timestamp = Long.valueOf(timestampTextField.getText().toString());
        final float lat = Float.valueOf(latitudeTextField.getText().toString());
        final float lng = Float.valueOf(longtitudeTextField.getText().toString());
        final String notes = notesTextField.getText().toString();


        if(!isUpdateScreen)
        {
            //add entry
            currentObservation = new Observation(timestamp, lat, lng, notes);

            ObservationsDatabase
                    .getDatabase(this)
                    .observationsDao()
                    .insert(currentObservation);
            //todo if picutres aviable, create inserts for them
        }
        else
        {
            //update entry
            currentObservation.timestamp = timestamp;
            currentObservation.lat = lat;
            currentObservation.lng = lng;
            currentObservation.notes = notes;

            ObservationsDatabase.getDatabase(this)
                    .observationsDao()
                    .update(currentObservation);

            //todo if pictures aviable, ceck if new one are possible/old ones got deleted

            ObservationsActivity.INSTANCE.finish();
        }

        startActivity(new Intent(this, ObservationsActivity.class));
        finish();
    }

    public void goBack(View v)
    {
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        if((getIntent().getStringExtra("updateEntryId")!=null))
        {
            isUpdateScreen = true;
        }

        if(isUpdateScreen)
        {
            TextView title = findViewById(R.id.screenTitle);
            title.setText("Update Entry");

            Button updateButton = findViewById(R.id.actionButton);
            updateButton.setText("Update Entry");

            try{
                currentObservation = loadObservationToUpdate();
                timestampTextField.setText(Long.toString(currentObservation.timestamp));
                latitudeTextField.setText(Float.toString(currentObservation.lat));
                longtitudeTextField.setText(Float.toString(currentObservation.lng));
                notesTextField.setText(currentObservation.notes);
            }
            catch (Exception e)
            {
                String errorMessage = "The following error occured while trying to open entry to update: "+e.getMessage();
                Log.e("updateEntry", errorMessage);

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }

        }
        else
        {
            InfoUpdate infoUpdate = new InfoUpdate(this);
            infoUpdate.start();
        }
    }

    private Observation loadObservationToUpdate() {
        return ObservationsDatabase
                .getDatabase(this)
                .observationsDao()
                .getElementById(Integer.valueOf(
                        getIntent().getStringExtra("updateEntryId")
                ));
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
        AddEditObservationActivity mainActivity;

        public InfoUpdate(AddEditObservationActivity mainActivity)
        {
            this.mainActivity = mainActivity;
        }

        public void run()
        {
            try {
                final IssLocation currentLocation = IssLiveData.GetIssLocation();

                if(timestampTextField.getText().toString().equals("")&&
                        latitudeTextField.getText().toString().equals("")&&
                        longtitudeTextField.getText().toString().equals(""))
                {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timestampTextField.setText(Integer.toString(currentLocation.getTimestamp()));
                            latitudeTextField.setText(Double.toString(currentLocation.getIssPosition().getLatitude()));
                            longtitudeTextField.setText(Double.toString(currentLocation.getIssPosition().getLongitude()));
                        }
                    });
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
