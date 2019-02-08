package isswatcher.manuelweb.at.Services.Models.Entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "pictures",
        foreignKeys = @ForeignKey(
                entity = Observation.class,
                parentColumns = "id",
                childColumns = "observation_id",
                onDelete = CASCADE
        )
)
public class Picture {
    public int observation_id;
    public String picture_id;
}
