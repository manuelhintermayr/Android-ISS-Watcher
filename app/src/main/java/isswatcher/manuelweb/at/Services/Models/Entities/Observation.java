package isswatcher.manuelweb.at.Services.Models.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "observations")
public class Observation {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;
    public float lat;
    public float lng;
    public String notes;

    public Observation(long timestamp, float lat, float lng, String notes) {
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
        this.notes = notes;
    }
}
