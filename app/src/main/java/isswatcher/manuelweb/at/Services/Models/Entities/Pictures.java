package isswatcher.manuelweb.at.Services.Models.Entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "pictures",
        foreignKeys = @ForeignKey(
                entity = Observations.class,
                parentColumns = "id",
                childColumns = "observation_id",
                onDelete = CASCADE
        )
)
public class Pictures {
    public int observation_id;
    public String picture_id;
}
