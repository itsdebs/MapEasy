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

    public void getRoadDistance(double lat1, double lng1, double lat2, double lng2, RoadDistanceCallback callback){
        String url = "https://maps.google.com/maps/api/directions/json?" +
                "origin=" + lat1 +"," + lng1 +
                "&destination=" + lat2 + "," + lng2 +
                "&sensor=false&units=metric&key=" + googlePathApiKey;
        new ConnectAsyncTask(callback, url).execute();
    }


    private class ConnectAsyncTask extends AsyncTask<Void, Void, Distance> {
        String url;
        RoadDistanceCallback callback;

        public ConnectAsyncTask(RoadDistanceCallback callback, String url) {
            this.callback = callback;
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }
        @Override
        protected Distance doInBackground(Void... params) {
            JsonParser jParser = new JsonParser();
            String json = jParser.getJSONFromUrl(url);
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement el = parser.parse(json);
            Distance d = new Distance();
            JsonObject jObj = el.getAsJsonObject()
                    .get("routes").getAsJsonArray()
                    .get(0).getAsJsonObject().
                            getAsJsonArray("legs")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("distance");
            d.text = jObj
                    .get("text")
//                    .get("value"); for getting distance in metres
                    .getAsString();
            d.value = jObj
                    .get("value")
                    .getAsFloat();
            return d;
        }
        @Override
        protected void onPostExecute(Distance result) {
            if(callback != null)
                callback.onRoadDistanceCalculated(result);
        }
    }

    public class Distance{
        public String text;
        public float value;
    }

    public interface RoadDistanceCallback{
        void onRoadDistanceCalculated(Distance distance);
    }
}