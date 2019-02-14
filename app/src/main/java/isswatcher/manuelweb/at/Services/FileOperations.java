package isswatcher.manuelweb.at.Services;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import isswatcher.manuelweb.at.R;

public class FileOperations {

    public static String getApplicationString(AppCompatActivity c)
    {
        //File.getExternalFilesDir
        return "/data/data/" + c.getPackageName() + File.separator + c.getString(R.string.app_name);
    }


    public static void createApplicationFolderIfDoesNotExist(AppCompatActivity c) {
        File applicationFolder = new File(getApplicationString(c));
        applicationFolder.mkdirs();
    }

    public static File createNewImageTempFile(AppCompatActivity c) throws IOException {

        File storageDir = new File(FileOperations.getApplicationString(c));

        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
}
