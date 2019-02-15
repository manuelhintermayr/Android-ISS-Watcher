package isswatcher.manuelweb.at.Services.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssPassResponse {

    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("risetime")
    @Expose
    private Integer risetime;

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRisetime() {
        return risetime;
    }

    public void setRisetime(Integer risetime) {
        this.risetime = risetime;
    }

}
