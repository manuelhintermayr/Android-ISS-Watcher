package isswatcher.manuelweb.at.UI.Activities;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.IssLocation;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
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
    private static final String GOOGLE_MAPS_SERVICES_TAG = "googleMapsService";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private List<Marker> markers = new ArrayList<Marker>();
    private GoogleMap mMap;
    private final Handler mHandler = new Handler();
    private Marker selectedMarker;
    private Animator animator = new Animator();

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

        setContentView(R.layout.activity_map);
        isGoogleMapsServiceOkay();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

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


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public boolean isGoogleMapsServiceOkay() {
        Log.d(GOOGLE_MAPS_SERVICES_TAG, "checking if google maps service is okay");
        int aviable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);
        if (aviable == ConnectionResult.SUCCESS) {
            Log.d(GOOGLE_MAPS_SERVICES_TAG, "google maps service is running");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(aviable)) {
            Log.e(GOOGLE_MAPS_SERVICES_TAG, "an error occured but it can be fixed by the user");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, aviable, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            String errorMessage = "Could not connect to google maps service";
            Log.e(GOOGLE_MAPS_SERVICES_TAG, errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }

        finish();
        return false;
    }

    public void addObservation(View v) {
        //get last location and send to addEditObservationActivity
        Intent intent = new Intent(this, AddEditObservationActivity.class);
        startActivity(intent);
    }

    public void goBack(View v) {
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
        IssAutoUpdate issAutoUpdate = new IssAutoUpdate(this);
        issAutoUpdate.start();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarkerToMap(latLng);
                animator.startAnimation(true);
            }
        });
    }

    protected Marker addMarkerToMap(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iss_pointer)));
        markers.add(marker);
        return marker;
    }

    public class IssAutoUpdate extends Thread {
        MapActivity mainActivity;
        public LatLng position;

        public IssAutoUpdate(MapActivity mainActivity)
        {
            this.mainActivity = mainActivity;
        }

        public void run()
        {
            for(int i=0;i<40;i++)
            {
                sleep();
                updatePosition();
                addNewMarker();
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

        public void updatePosition()
        {
            try {
                IssLocation currentLocation = IssLiveData.GetIssLocation();
                position = new LatLng(
                        currentLocation.getIssPosition().getLatitude(),
                        currentLocation.getIssPosition().getLongitude()
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addNewMarker()
        {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMarkerToMap(position);

                    //start - remove afterwards
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                    //end - remove afterwards
                    //replace with inside method of animation
                }
            });
        }
    }


    public class Animator implements Runnable {
        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;
        private final Interpolator interpolator = new LinearInterpolator();
        int currentIndex = 0;
        float tilt = 90;
        float zoom = 15.5f;
        boolean upward = true;
        long start = SystemClock.uptimeMillis();
        LatLng endLatLng = null;
        LatLng beginLatLng = null;
        boolean showPolyline = false;
        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        private void resetMarkers() {
            for (Marker marker : markers) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.iss_pointer));
            }
        }

        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);
        }

        private void highLightMarker(int index) {
            highLightMarker(markers.get(index));
        }

        private void highLightMarker(Marker marker) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.iss_pointer_disabled));
            marker.showInfoWindow();
            selectedMarker = marker;
        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;
            highLightMarker(0);
            if (showPolyLine) {
                polyLine = initializePolyLine();
            }
            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();
            setupCameraPositionForMovement(markerPos, secondPos);
        }

        private void setupCameraPositionForMovement(LatLng markerPos, LatLng secondPos) {
            float bearing = bearingBetweenLatLngs(markerPos, secondPos);
            trackingMarker = mMap.addMarker(new MarkerOptions().position(markerPos)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.iss_pointer))
                    .title("IS Station")
                    .snippet("ISS Position"));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(markerPos)
                    .bearing(bearing + BEARING_OFFSET)
                    .tilt(90)
                    .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16)
                    .build();

            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            Log.e("animator before reset", animator + "");
                            animator.reset();
                            Log.e("animator after reset", animator + "");
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    });
        }

        public void navigateToPoint(LatLng latLng, boolean animate) {
            CameraPosition position = new CameraPosition.Builder().target(latLng).build();
            changeCameraPosition(position, animate);
        }

        private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            if (animate) {
                mMap.animateCamera(cameraUpdate);
            } else {
                mMap.moveCamera(cameraUpdate);
            }
        }

        private Location convertLatLngToLocation(LatLng latLng) {
            Location loc = new Location("someLoc");
            loc.setLatitude(latLng.latitude);
            loc.setLongitude(latLng.longitude);
            return loc;
        }

        private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
            Location beginL = convertLatLngToLocation(begin);
            Location endL = convertLatLngToLocation(end);
            return beginL.bearingTo(endL);
        }

        public void toggleStyle() {
            if (GoogleMap.MAP_TYPE_NORMAL == mMap.getMapType()) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();

        private Polyline initializePolyLine() {
            rectOptions.add(markers.get(0).getPosition());
            return mMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }

        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size() > 2) {
                animator.initialize(showPolyLine);
            }
        }

        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);
            Log.w("interpolator", t + "");
            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            Log.w("lat. lng", lat + "," + lng + "");
            LatLng newPosition = new LatLng(lat, lng);
            Log.w("newPosition", newPosition + "");

            trackingMarker.setPosition(newPosition);
            if (showPolyline) {
                updatePolyLine(newPosition);
            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {
                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < markers.size() - 2) {
                    currentIndex++;
                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();
                    start = SystemClock.uptimeMillis();
                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();
                    float bearingL = bearingBetweenLatLngs(begin, end);
                    highLightMarker(currentIndex);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(end) // changed this...
                            .bearing(bearingL + BEARING_OFFSET)
                            .tilt(tilt)
                            .zoom(mMap.getCameraPosition().zoom)
                            .build();
                    mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );
                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);
                } else {
                    currentIndex++;
                    highLightMarker(currentIndex);
                    stopAnimation();
                }
            }
        }

        private LatLng getEndLatLng() {
            return markers.get(currentIndex + 1).getPosition();
        }

        private LatLng getBeginLatLng() {
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {
            if (upward) {
                if (tilt < 90) {
                    tilt++;
                    zoom -= 0.01f;
                } else {
                    upward = false;
                }
            } else {
                if (tilt > 0) {
                    tilt--;
                    zoom += 0.01f;
                } else {
                    upward = true;
                }
            }
        }
    }

    ;

    private void loadMap() {
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        } catch (Exception e) {
            e.toString();
        }
    }
}
