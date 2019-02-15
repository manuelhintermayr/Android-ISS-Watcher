package isswatcher.manuelweb.at.Services.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssLocation {

    @SerializedName("iss_position")
    @Expose
    private IssPosition issPosition;
    @SerializedName("timestamp")
    @Expose
    private Integer timestamp;
    @SerializedName("message")
    @Expose
    private String message;

    public IssPosition getIssPosition() {
        return issPosition;
    }

    public void setIssPosition(IssPosition issPosition) {
        this.issPosition = issPosition;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}