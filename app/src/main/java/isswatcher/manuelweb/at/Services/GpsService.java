package isswatcher.manuelweb.at.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class GpsService extends Service {
    private LocationListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
