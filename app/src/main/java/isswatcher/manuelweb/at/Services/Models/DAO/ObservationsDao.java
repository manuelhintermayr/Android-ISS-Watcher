package isswatcher.manuelweb.at.Services.Models.DAO;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;

@Dao
public interface ObservationsDao {

    @Insert
    void insert(Observation entry);

    @Query("SELECT * FROM observations ORDER BY timestamp")
    List<Observation> getAllEntries();

    @Query("SELECT * FROM observations WHERE id = :id")
    Observation getElementById(int id);

    @Update
    void update(Observation entry);

    @Delete
    void delete(Observation entry);

}
