package isswatcher.manuelweb.at.Services.Models.DAO;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.Models.Entities.Picture;

@Dao
public interface PictureDao {
    @Insert
    void insert(Picture entry);

    List<Observation> getAllEntries();

    @Delete
    void delete(Picture entry);

}
