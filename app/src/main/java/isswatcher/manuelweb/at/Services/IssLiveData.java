package isswatcher.manuelweb.at.Services;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import isswatcher.manuelweb.at.Services.Models.IssLocation;
import isswatcher.manuelweb.at.Services.Models.IssPeople;

public class IssLiveData {
    public static IssLocation GetIssLocation() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request requestLocation = new Request.Builder().url("http://api.open-notify.org/iss-now.json").build();
        Response response = null;
        response = client.newCall(requestLocation).execute();
        String resultJson = response.body().string();

        Gson gson = new Gson();
        return gson.fromJson(resultJson, IssLocation.class);
    }

    public static IssPeople GetPeopleOnIss() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request requestLocation = new Request.Builder().url("http://api.open-notify.org/astros.json").build();
        Response response = null;
        response = client.newCall(requestLocation).execute();
        String resultJson = response.body().string();

        Gson gson = new Gson();
        return gson.fromJson(resultJson, IssPeople.class);
    }

    public static IssPeople GetNextFiveTimeIssPasses() throws IOException {
    //public static IssPeople GetNextFiveTimeIssPasses(double lat, double lon) throws IOException {
        double lat = 53.766700;
        double lon = -2.708990;

        OkHttpClient client = new OkHttpClient();
        Request requestLocation = new Request.Builder().url("http://api.open-notify.org/iss-pass.json?lat="+lat+"&lon="+lon).build();
        Response response = null;
        response = client.newCall(requestLocation).execute();
        String resultJson = response.body().string();

        Gson gson = new Gson();
        return gson.fromJson(resultJson, IssPeople.class);
    }
}
