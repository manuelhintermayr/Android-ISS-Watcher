package isswatcher.manuelweb.at.Services.Models.DAO;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;

@Dao
public interface ObservationsDao {

    @Insert
    void insert(Observation entry);

    @Query("SELECT * FROM observations ORDER BY timestamp")
    List<Observation> getAllEntries();

    @Delete
    void delete(Observation entry);

}