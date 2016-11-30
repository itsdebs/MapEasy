package com.vagabond.mapeasy.maphandler;
import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Debanjan Chatterjee on 29/4/16.
 */
public class RoadDistance {

    private String googlePathApiKey;

    public RoadDistance(String googlePathApiKey) {
        this.googlePathApiKey = googlePathApiKey;
    }
    //    private Context context;

    /*@Inject
    public RoadDistance(Context context) {
//        this.context = context;
    }*/

    public void getRoadDistanceAndTime(double lat1, double lng1, double lat2, double lng2,
                                       RoadDistanceAndTimeCallback callback){
        String url = "https://maps.google.com/maps/api/directions/json?" +
                "origin=" + lat1 +"," + lng1 +
                "&destination=" + lat2 + "," + lng2 +
                "&sensor=false&units=metric&key=" + googlePathApiKey;
        new ConnectAsyncTask(callback, url).execute();
    }


    private class ConnectAsyncTask extends AsyncTask<Void, Void, DistanceAndTime> {
        String url;
        RoadDistanceAndTimeCallback callback;

        public ConnectAsyncTask(RoadDistanceAndTimeCallback callback, String url) {
            this.callback = callback;
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }
        @Override
        protected DistanceAndTime doInBackground(Void... params) {
            JsonParser jParser = new JsonParser();
            String json = jParser.getJSONFromUrl(url);
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement el = parser.parse(json);
            DistanceAndTime d = new DistanceAndTime();
            JsonObject jObj = el.getAsJsonObject()
                    .get("routes").getAsJsonArray()
                    .get(0).getAsJsonObject().
                            getAsJsonArray("legs")
                    .get(0).getAsJsonObject();
            JsonObject distObj =jObj.getAsJsonObject("distance");
            d.distText = distObj
                    .get("text")
//                    .get("distValue"); for getting distance in metres
                    .getAsString();
            d.distValue = distObj
                    .get("value")
                    .getAsFloat();
            JsonObject timeObj =jObj.getAsJsonObject("duration");
            d.timeText = timeObj
                    .get("text")
//                    .get("distValue"); for getting distance in metres
                    .getAsString();
            d.timeValue = timeObj //seconds
                    .get("value")
                    .getAsFloat();
            return d;
        }
        @Override
        protected void onPostExecute(DistanceAndTime result) {
            if(callback != null)
                callback.onRoadDistanceCalculated(result);
        }
    }
/*
eg:
* "duration" : {
                  "text" : "46 mins",
                  "value" : 2769
               }
* */
    public class DistanceAndTime {
        public String distText;
        public float distValue;
        public String timeText;
        public float timeValue;
    }

    public interface RoadDistanceAndTimeCallback {
        void onRoadDistanceCalculated(DistanceAndTime distance);
    }
}