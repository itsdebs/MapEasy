package com.vagabond.mapeasy.maphandler;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



/**
 * Created by Debanjan on 19/1/16.
 */
public class FindPlace  {

    private Context context;
    private AddressReceivedListener listener;


    public FindPlace(Context context) {
        this.context = context;
    }

    public synchronized void  findAddressForLocation(final double[] locs, AddressReceivedListener listener){
        this.listener = listener;
        new FindAddress(listener).execute(locs);

//        this.execute(locs);
    }

    private class FindAddress extends AsyncTask<double[], Void, List<Address>>{
        AddressReceivedListener listener;

        public FindAddress(AddressReceivedListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<Address> doInBackground(double[]... params) {
            List<Address> addresses = null;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation((params[0])[0], (params[0])[1], 1);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(listener != null && addresses != null && !addresses.isEmpty()){


                // location result to returned, by documents it recommended 1 to 5
                StringBuilder sb = new StringBuilder();
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                String city = addresses.get(0).getLocality();
                String locality = addresses.get(0).getSubLocality();

                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();


                if (knownName != null)
                    sb.append(knownName + ",");
                if (locality != null)
                    sb.append(locality + ", ");
                if (city != null)
                    sb.append(city + ", ");
                if (state != null)
                    sb.append(state + ", ");
                if (country != null)
                    sb.append(country );
                if (postalCode != null)
                    sb.append( "-" + postalCode + ".");

                listener.onAdressReceived(sb.toString());




            }

        }
    }




    public interface AddressReceivedListener{
        void onAdressReceived(String add);
    }
}