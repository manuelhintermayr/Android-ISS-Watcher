package isswatcher.manuelweb.at.Services;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import isswatcher.manuelweb.at.Services.Models.IssLocation;

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
}
