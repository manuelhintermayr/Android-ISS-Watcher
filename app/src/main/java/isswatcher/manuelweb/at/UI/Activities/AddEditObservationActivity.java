package isswatcher.manuelweb.at.UI.Activities;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.FileOperations;
import isswatcher.manuelweb.at.Services.IssLiveData;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.Models.Entities.Picture;
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
    public static final int PICK_IMAGE = 123;
    private static final int REQUEST_CAPTURE_IMAGE = 124;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private TextInputEditText timestampTextField;
    private TextInputEditText latitudeTextField;
    private TextInputEditText longtitudeTextField;
    private TextInputEditText notesTextField;
    private TextView countSelectedImages;
    private Button addImageButton;
    private Button removeAllImagesButton;
    private ImageView currentImage;
    private String lastPicturePath;
    private boolean isUpdateScreen = false;
    private boolean pressedRemoveAllPictures = false;
    private Observation currentObservation;
    private List<Picture> pictureList;
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
        countSelectedImages = findViewById(R.id.countSelectedImages);
        addImageButton = findViewById(R.id.addImageButton);
        removeAllImagesButton = findViewById(R.id.removeAllImagesButton);
        currentImage = findViewById(R.id.currentImage);
        pictureList = new ArrayList<Picture>();
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
        countSelectedImages.setPaintFlags(countSelectedImages.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        removeAllImages(removeAllImagesButton);
        pressedRemoveAllPictures = false;
    }

    public void addImage(View v)
    {
        if (ContextCompat.checkSelfPermission(AddEditObservationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder
                    .setTitle("Image")
                    .setMessage("Choose the image location")
                    .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            addImageFromGallery();
                        }
                    })
                    .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            addImageFromCamera();
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
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String infoMessage = "Permissions granted!";
                    Log.d("permissions", infoMessage);
                    Toast.makeText(this, infoMessage, Toast.LENGTH_LONG).show();
                } else {
                    String errorMessage = "Cannot add image without permission.";
                    Log.e("permissions", errorMessage);
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void addImageFromGallery()
    {
        //got from https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void addImageFromCamera()
    {
        //https://android.jlelse.eu/androids-new-image-capture-from-a-camera-using-file-provider-dd178519a954
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = FileOperations.createNewImageTempFile(this);
                lastPicturePath = photoFile.getAbsolutePath();
            }
            catch (IOException e) {
                e.printStackTrace();
                String errorMessage = "The following error occured: "+e.getMessage();

                Log.e("savingImage", errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            Uri photoUri = FileProvider.getUriForFile(AddEditObservationActivity.this, AddEditObservationActivity.this.getPackageName() +".provider", photoFile);

            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = Uri.parse(lastPicturePath);
            addImage(imageUri);
        }

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri originalImageUri = data.getData();
            File newFile = null;
            try {
                newFile = FileOperations.copyFileIntoStorage(FileOperations.getRealPathFromUri(this, originalImageUri), this);

                Uri imageUri = Uri.parse(newFile.getAbsolutePath());
                addImage(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                String errorMessage = "The following error occured: "+e.getMessage();

                Log.e("savingImage", errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addImage(Uri imageUri)
    {
        currentImage.setImageURI(imageUri);

        pictureList.add(new Picture(imageUri.toString()));

        updateImageButtons();
    }

    private void updateImageButtons()
    {
        if(pictureList.size()>0)
        {
            removeAllImagesButton.setTextColor(ContextCompat.getColor(this, R.color.black_overlay));
            removeAllImagesButton.setEnabled(true);
            countSelectedImages.setText(Integer.toString(pictureList.size()));
        }
    }


    public void removeAllImages(View v)
    {
        currentImage.setImageResource(R.drawable.iss_64);
        pictureList.clear();
        countSelectedImages.setText("0");
        removeAllImagesButton.setTextColor(Color.WHITE);
        removeAllImagesButton.setEnabled(false);
        pressedRemoveAllPictures = true;
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

            int newObservationId = getObservationByTimestamp(currentObservation.timestamp).id;

            addCurrentImageListToDatabase(newObservationId);
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

            if(pressedRemoveAllPictures)
            {
                List<Picture> oldPictureList = getPictureListFromDatabase(currentObservation.id);
                if(oldPictureList.size()>0)
                {
                    //remove old entries
                    for(int i=0;i<oldPictureList.size();i++)
                    {
                        Picture currentOldPicture = oldPictureList.get(i);

                        ObservationsDatabase.getDatabase(this)
                                .pictureDao()
                                .delete(currentOldPicture);
                    }

                    //add new entries
                    addCurrentImageListToDatabase(currentObservation.id);
                }
            }
            else{
                //add missing entries
                addCurrentImageListToDatabase(currentObservation.id);
            }

            ObservationsActivity.INSTANCE.finish();
        }

        startActivity(new Intent(this, ObservationsActivity.class));
        finish();
    }

    private void addCurrentImageListToDatabase(int newObservationId) {
        if(pictureList.size()>0)
        {
            for(int i=0;i<pictureList.size();i++)
            {
                Picture currentPicture = pictureList.get(i);
                if(currentPicture.observation_id==-1)
                {
                    currentPicture.observation_id = newObservationId;

                    ObservationsDatabase
                            .getDatabase(this)
                            .pictureDao()
                            .insert(currentPicture);
                }
            }
        }
    }

    private void cleanUpOldNotUsedPictures() {
        List<Picture> totalPictureList = ObservationsDatabase
                .getDatabase(this)
                .pictureDao()
                .getAllEntries();
        boolean picturesInDatabase = totalPictureList.size()>0 ? true : false;
        List<String> usedPictureNames = null;

        if(picturesInDatabase)
        {
            Picture[] totalPictureArray = new Picture[totalPictureList.size()];
            totalPictureList.toArray(totalPictureArray);
            Object[] result = Arrays.stream(totalPictureArray).map(o -> convertUriStringToFileName(o.picture_id)).toArray();
            String[] pictureNameArray = Arrays.copyOf(result, result.length, String[].class);

            usedPictureNames = Arrays.asList(pictureNameArray);
        }

        File[] imagesOnStorage = FileOperations.getStorageDirectory(AddEditObservationActivity.this).listFiles();
        if(imagesOnStorage.length>0)
        {
            for(int i=0;i<imagesOnStorage.length;i++)
            {
                File currentFile = imagesOnStorage[i];
                String fileName = currentFile.getName();
                if(picturesInDatabase)
                {
                    if(!usedPictureNames.contains(fileName))
                    {
                        boolean result = currentFile.delete();
                        if(!result)
                        {
                            throwCannotDeleteFileErrorMessage(currentFile);
                        }
                    }
                }
                else
                {
                    boolean result = currentFile.delete();
                    if(!result)
                    {
                        throwCannotDeleteFileErrorMessage(currentFile);
                    }
                }
            }
        }
    }

    private void throwCannotDeleteFileErrorMessage(File file) {
        AddEditObservationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String errorMessage = "Could not delete the following file: "+file.getAbsolutePath();
                Log.e("deleteFile", errorMessage);

                Toast.makeText(AddEditObservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String convertUriStringToFileName(String uriString)
    {
        return Paths.get(uriString).getFileName().toString();
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
                loadPicturesForId(currentObservation.id);
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

    private void loadPicturesForId(int observationId) {

        pictureList = getPictureListFromDatabase(observationId);
        if(pictureList.size()>0)
        {
            updateImageButtons();
            currentImage.setImageURI(Uri.parse(pictureList.get(pictureList.size()-1).picture_id));
        }
    }

    private List<Picture> getPictureListFromDatabase(int observationId)
    {
        return ObservationsDatabase
                .getDatabase(this)
                .pictureDao()
                .getEntriesByObservationId(observationId);
    }

    private Observation loadObservationToUpdate() {
        return ObservationsDatabase
                .getDatabase(this)
                .observationsDao()
                .getElementById(Integer.valueOf(
                        getIntent().getStringExtra("updateEntryId")
                ));
    }

    private Observation getObservationByTimestamp(long timestamp)
    {
        return ObservationsDatabase
                .getDatabase(this)
                .observationsDao()
                .getElementByTimestamp(timestamp);
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

            cleanUpOldNotUsedPictures();
        }
    }
}
