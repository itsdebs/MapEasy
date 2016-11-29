package com.vagabond.mapeasy.maphandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Debanjan on 8/1/16.
 */
class PathToDestination {

    private GoogleMap mMap;
    private Context context;
    private static final String API_KEY = "AIzaSyB34zbuvnUfYKmczJZXZ5yHJ2O-kO39VvI";

    private String pathColor = "#05b1fb";//default blue color
    private int pathWidth = 5;//default is 5
    public PathToDestination(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }

    public void setPathColor(String pathColor) {
        this.pathColor = pathColor;
    }

    public void setPathWidth(int pathWidth) {
        this.pathWidth = pathWidth;
    }

    public void drawPathBetween(double sourcelat, double sourcelog, double destlat, double destlog) {
        drawPathBetween(sourcelat, sourcelog, destlat, destlog, null);
    }

    public void drawPathBetween(double sourcelat, double sourcelog, double destlat, double destlog, MapModel[] waypoints) {
        String urlStr = makeURL(sourcelat, sourcelog, destlat, destlog, waypoints);
        new ConnectAsyncTask(urlStr).execute();
    }

    private String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        return makeURL(sourcelat, sourcelog, destlat, destlog, null);
    }

    private String makeURL(double sourcelat, double sourcelog, double destlat, double destlog, MapModel... waypoints) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        if (waypoints != null && waypoints.length > 0) {
            urlString.append("&waypoints=");// waypoints
            for (int i = 0; i < waypoints.length; i++) {
                MapModel coordinate = waypoints[i];
                urlString.append(coordinate.getLatitude());
                urlString.append(",");
                urlString.append(coordinate.getLongitude());
                if (i < waypoints.length - 1) {
                    urlString.append("|");
                }
            }
        }
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=true");
        urlString.append("&key=" + API_KEY);
        return urlString.toString();
    }

    private void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(pathWidth)
                    .color(Color.parseColor(pathColor))//Google maps blue color
                    .geodesic(true)
            );
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void drawPathFromEncodedPolyline(String encodedPolyline) {

//        try {
        //Tranform the string into a json object
//            final JSONObject json = new JSONObject(result);
//            JSONArray routeArray = json.getJSONArray("routes");
//            JSONObject routes = routeArray.getJSONObject(0);
//            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
//            String encodedString = overviewPolylines.getString("points");
        List<LatLng> list = decodePoly(encodedPolyline);
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .addAll(list)
                .width(5)
                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                .geodesic(true)
        );
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
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


    private class ConnectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        ConnectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                    !((Activity) context).isDestroyed()) ||
                    !((Activity) context).isFinishing()) {
                progressDialog = new ProgressDialog(context);
//                progressDialog.setMessage("Fetching route, Please wait...");
//                progressDialog.setIndeterminate(true);
//                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                URL url = new URL(this.url);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                return jsonResults.toString();
            } catch (IOException e) {
                return jsonResults.toString();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return jsonResults.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.hide();
            }

            if (result != null) {
                drawPath(result);
            }
        }
    }

}