package com.example.miljanamilena.pathfinder;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by MiljanaMilena on 2/23/2018.
 */

public class FetchUrl extends AsyncTask<String, Void, String> {

    private GoogleMap map;
    private TextView distanceCovered, locationDistance;
    private LinearLayout layoutInfo;
    private int request;
    private Dialog dialog;


    public FetchUrl (GoogleMap googleMap, TextView location, LinearLayout info, Dialog loading)
    {
        this.map = googleMap;
        this.locationDistance = location;
        this.layoutInfo = info;
        this.request = 1;
        this.dialog = loading;
    }
    public FetchUrl (GoogleMap googleMap, TextView distance)
    {
        this.map = googleMap;
        this.distanceCovered = distance;
        this.request = 2;
    }

    @Override
    protected String doInBackground(String... url) {

        // For storing data from web service
        String data = "";

        try
        {
            // Fetching the data from web service
            data = downloadUrl(url[0]);
            Log.d("Background Task data", data.toString());
        }
        catch (Exception e)
        {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(20000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);

            br.close();

        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
        }
        finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        switch (request)
        {
            case 1:
                ParserTask parserTask1 = new ParserTask(map, locationDistance, layoutInfo, dialog);
                parserTask1.execute(result);
                break;

            case 2:
                ParserTask parserTask2 = new ParserTask(map, distanceCovered);
                parserTask2.execute(result);
                break;
        }

    }
}
