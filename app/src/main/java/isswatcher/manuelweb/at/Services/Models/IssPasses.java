package isswatcher.manuelweb.at.Services.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssPasses {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("request")
    @Expose
    private IssPassRequest request;
    @SerializedName("response")
    @Expose
    private List<IssPassResponse> response = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public IssPassRequest getRequest() {
        return request;
    }

    public void setRequest(IssPassRequest request) {
        this.request = request;
    }

    public List<IssPassResponse> getResponse() {
        return response;
    }

    public void setResponse(List<IssPassResponse> response) {
        this.response = response;
    }

}