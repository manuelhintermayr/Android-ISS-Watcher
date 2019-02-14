package isswatcher.manuelweb.at.Services.Models.Entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

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
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String picture_id;

    public Picture(@NonNull String picture_id) {
        this.picture_id = picture_id;
        this.observation_id = -1;
    }
}


