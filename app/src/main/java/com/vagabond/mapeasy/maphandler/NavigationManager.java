package com.vagabond.mapeasy.maphandler;

/**
 * Created by debanjan on 29/11/16.
 */
import android.content.Intent;
import android.net.Uri;

public class NavigationManager {
    public static Intent startNaigationTo(double lat, double lng){
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(lat)
                +"," + String.valueOf(lng) + "&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        return mapIntent;

    }

}

