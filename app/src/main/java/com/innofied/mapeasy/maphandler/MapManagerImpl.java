package com.innofied.mapeasy.maphandler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.innofied.mapeasy.maphandler.exceptions.LocationRequestNotEnabledException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by debanjan on 8/11/16.
 */

public class MapManagerImpl implements MapManager, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap map;
    private int leftPad = 0, rightPad = 0, topPad = 0, bottomPad = 0;
    private Context context;

    private LatLng userLatlng = null;
    private Location userLocation = null;

    private View markerView = null;
    private Map<Marker, MapModel> markerMap;
    private boolean isLocationRequestPossible, showMarkerWindow = false;
    private
    @DrawableRes
    int userIcon;
    private GoogleApiClient googleApiClient;

    private List<OnGoogleApiConnectedCallback> googleApiConnectedCallbacks;

    private boolean isGoogleApiClientReady;

    public MapManagerImpl(Context context) {
        this.context = context;
        markerMap = new HashMap<>();
        googleApiConnectedCallbacks = new ArrayList<>();
    }

    @Override
    public void setPaddingToMap(int left, int right, int top, int bottom) {
        leftPad = left;
        rightPad = right;
        topPad = top;
        bottomPad = bottom;
    }


    protected boolean isGoogleApiClientReady() {
        return isGoogleApiClientReady;
    }

    @Override
    public void makeMapready(GoogleMap map) {
        this.map = map;
        map.setPadding(leftPad, topPad, rightPad, bottomPad);
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();

    }

    @Override
    public void setMyLocationEnabled(boolean isLocationEnabled) throws LocationRequestNotEnabledException{
        if (map == null)
            return;
        if(!isLocationRequestPossible)
            throw new LocationRequestNotEnabledException();
        else
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map.setMyLocationEnabled(isLocationEnabled);

    }


    @Override
    public void addMarkers(final boolean showonMapMandatory,
                           final boolean showWithMyPosition, @DrawableRes final int icon,
                           final MapModel... mapModels) {
        if(isGoogleApiClientReady()) {
            addMarkersOnReady(showonMapMandatory, showWithMyPosition, icon, mapModels);
        }
        else {
            googleApiConnectedCallbacks.add(new OnGoogleApiConnectedCallback() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    addMarkersOnReady(showonMapMandatory, showWithMyPosition, icon, mapModels);
                }

                @Override
                public void onSuspended(int cause) {

                }
            });
        }
    }

    protected void addMarkersOnReady(boolean showonMapMandatory,
                                     boolean showWithMyPosition, @DrawableRes int icon,
                                     MapModel... mapModels){
        LatLngBounds.Builder builder = null;
        builder = new LatLngBounds.Builder();
        if (showWithMyPosition && userLatlng != null) {
            builder.include(userLatlng);
        }

        for (MapModel m : mapModels) {
            LatLng ll = new LatLng(m.getLatitude(), m.getLongitude());
            if (showonMapMandatory) {
                builder.include(ll);
            }
            Marker mo = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .icon(BitmapDescriptorFactory.fromResource(icon))
            );

            markerMap.put(mo, m);
        }
        if (userIcon <= 0) {
            map.addMarker(new MarkerOptions()
                    .position(userLatlng).icon(BitmapDescriptorFactory.fromResource(userIcon)));
        }
        if (showonMapMandatory || showWithMyPosition) {
            LatLngBounds latLngBounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (60
                    * context.getResources().getDisplayMetrics().density));
            moveOrAnimateCamera(cu);
        }
    }



    @Override
    public void clearMarkers() {
        map.clear();
        markerMap.clear();
    }


    protected void moveOrAnimateCamera(CameraUpdate cu) {
        map.moveCamera(cu);
    }

    @Override
    public void setUserIcon(@DrawableRes int icon) {
        userIcon = icon;
    }

    @Override
    public void setMarkerWindow(View view) {
        markerView = view;
    }

    @Override
    public void showMarkerWindow(boolean show) {
        showMarkerWindow = show;
    }

    @Override
    public void setMarkerWindowClickListener(MarkerWindowClickedListener markerWindowClickedListener) {

    }

    @Override
    public void drawPathBetween(@PathMode int mode, MapModel... mapModels) {

    }

    @Override
    public void drawPathBetween(@PathMode int mode, MapModel start, MapModel end, MapModel[] waypoints) {

    }

    @Override
    public void gotoMyLocation(boolean isAnimate) {
        gotoMyLocation(isAnimate, 0);
    }

    @Override
    public void gotoLocation(MapModel mapModel, boolean isAnimate) {
        gotoLocation(mapModel, isAnimate, 0);
    }

    @Override
    public void gotoMyLocation(boolean isAnimate, float zoom) {
        gotoLocation(userLatlng,isAnimate, zoom);
    }

    @Override
    public void gotoLocation(MapModel mapModel, boolean isAnimate, float zoom) {
        gotoLocation(new LatLng(mapModel.getLatitude(), mapModel.getLongitude()), isAnimate, zoom);
    }

    //set 0 as zoom level if not required
    protected void gotoLocation(LatLng latLng, boolean isAnimate, float zoom){
        CameraUpdate cu =zoom > 0? CameraUpdateFactory.newLatLngZoom(latLng,zoom):CameraUpdateFactory.newLatLng(latLng);
        if(isAnimate){
            map.animateCamera(cu);
        }
        else
            map.moveCamera(cu);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }googleApiConnectedCallbacks.clear();
        userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        userLatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        if(googleApiConnectedCallbacks != null && !googleApiConnectedCallbacks.isEmpty()){
            for (OnGoogleApiConnectedCallback cb: googleApiConnectedCallbacks) {
                cb.onConnected(bundle);
            }
            googleApiConnectedCallbacks.clear();


        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        if(googleApiConnectedCallbacks != null && !googleApiConnectedCallbacks.isEmpty()){
            for (OnGoogleApiConnectedCallback cb: googleApiConnectedCallbacks) {
                cb.onSuspended(cause);
            }
            googleApiConnectedCallbacks.clear();
        }
    }

    public void createLocationRequest(final ActivityCallback activityCallback) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(600);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        isLocationRequestPossible = true;
                        activityCallback.onLocationRequestSatisfied();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        activityCallback.onLocationSettingsNotSatisfied(status);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        activityCallback.onLocationSettingsUnavialable();
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected interface OnGoogleApiConnectedCallback{
        void onConnected(@Nullable Bundle bundle);
        void onSuspended(int cause);
    }

    public interface ActivityCallback{
        //call other methods
        void onLocationRequestSatisfied();
        /*
        * Use the following code in activity
        * try {
                     // Show the dialog by calling startResolutionForResult(),
                     // and check the result in onActivityResult().
                     status.startResolutionForResult(
                         NamedActivity.this,
                         REQUEST_CHECK_SETTINGS);
                 } catch (SendIntentException e) {
                     // Ignore the error.
                 }

        * */

        void onLocationSettingsNotSatisfied(Status status);
        void onLocationSettingsUnavialable();
    }
    protected boolean onMarkerClickCompleted(){
        return false;
    }
    protected boolean onMarkerClicked(Marker marker){
        return onMarkerClicked(marker, markerMap.get(marker));
    }
    protected boolean onMarkerClicked(Marker marker, MapModel mapModel){
        gotoLocation(mapModel, true);
        return onMarkerClickCompleted();
    }

    private class MarkerClickListener implements GoogleMap.OnMarkerClickListener{

        @Override
        public boolean onMarkerClick(Marker marker) {

            return onMarkerClick(marker);
        }
    }
}
