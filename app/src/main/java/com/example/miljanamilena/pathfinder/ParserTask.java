package com.example.miljanamilena.pathfinder;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MiljanaMilena on 2/23/2018.
 */

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    private GoogleMap googleMap;
    private TextView distanceCovered, locationDistance;
    private LinearLayout layoutInfo;
    private String curentDistance;
    private int request;


    public ParserTask(GoogleMap map, TextView location, LinearLayout info)
    {
        this.googleMap = map;
        this.locationDistance = location;
        this.layoutInfo = info;
        this.request = 1;
    }
    public ParserTask(GoogleMap map, TextView distance)
    {
        this.googleMap = map;
        this.distanceCovered = distance;
        this.request = 2;
    }
    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try
        {
            jObject = new JSONObject(jsonData[0]);
            Log.d("ParserTask",jObject.toString());

            // Starts parsing data
            routes = parse(jObject);
            Log.d("ParserTask","Executing routes");
            Log.d("ParserTask",routes.toString());
        }
        catch (Exception e)
        {
            Log.d("ParserTask",e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try
        {
            System.out.println("JOBJECT "+ jObject.toString());

            jRoutes = jObject.getJSONArray("routes");
            System.out.println("JROUTES "+ jRoutes.toString());
            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                //System.out.println("LEGS "+ jLegs.toString());
                curentDistance = ((JSONObject)((JSONObject)jLegs.get(i)).get("distance")).get("value").toString();
                List path = new ArrayList<>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
                    System.out.println("STEPS "+ jSteps.toString());

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = ((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points").toString();
                        System.out.println("POLyLINE "+ polyline);
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude) );
                            hm.put("lng", Double.toString((list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);

            Log.d("onPostExecute","onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null)
        {
            googleMap.addPolyline(lineOptions);
            if (request == 1)
            {
                locationDistance.setText(curentDistance);
                layoutInfo.setVisibility(View.VISIBLE);
            }
            else if (request == 2)
            {
                long distance = Long.parseLong(distanceCovered.getText().toString());
                long curDist = Long.parseLong(curentDistance);
                distance = distance + curDist;
                distanceCovered.setText(String.valueOf(distance));
            }
        }
        else
        {
            Log.d("onPostExecute","without Polylines drawn");
        }
    }
}
