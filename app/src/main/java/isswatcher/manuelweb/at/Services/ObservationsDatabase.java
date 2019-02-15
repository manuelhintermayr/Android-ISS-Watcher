package isswatcher.manuelweb.at.Services;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import isswatcher.manuelweb.at.Services.Models.DAO.ObservationsDao;
import isswatcher.manuelweb.at.Services.Models.DAO.PictureDao;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.Models.Entities.Picture;

@Database(entities = { Observation.class, Picture.class },
        version = 1)
public abstract class ObservationsDatabase extends RoomDatabase {

    public abstract ObservationsDao observationsDao();
    public abstract PictureDao pictureDao();

    private static volatile ObservationsDatabase INSTANCE;

    public static ObservationsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ObservationsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ObservationsDatabase.class, "observation_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
