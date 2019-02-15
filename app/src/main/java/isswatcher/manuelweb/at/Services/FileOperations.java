package isswatcher.manuelweb.at.Services;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class FileOperations {

    public static File getStorageDirectory(AppCompatActivity c)
    {
        return c.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public static String getNewTempFileName()
    {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "iss_" + timeStamp + "_";
        return imageFileName;
    }

    public static File createNewImageTempFile(AppCompatActivity c) throws IOException {

        File storageDir = getStorageDirectory(c);
        File image = File.createTempFile(getNewTempFileName(), ".jpg", storageDir);

        return image;
    }

    public static File copyFileIntoStorage(String sourceFile, AppCompatActivity c) throws IOException {
        File source = new File(sourceFile);

        String destinationPath = getStorageDirectory(c)+"/"+getNewTempFileName()+".jpg";
        File destination = new File(destinationPath);

        FileUtils.copyFile(source, destination);

        return destination;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        //https://stackoverflow.com/questions/20028319/how-to-convert-content-media-external-images-media-y-to-file-storage-sdc
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
