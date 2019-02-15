package isswatcher.manuelweb.at.Services.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssPeople {

    @SerializedName("people")
    @Expose
    private List<Person> people = null;
    @SerializedName("number")
    @Expose
    private Integer number;
    @SerializedName("message")
    @Expose
    private String message;

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}