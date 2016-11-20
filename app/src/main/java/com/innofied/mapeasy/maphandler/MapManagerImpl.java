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
import com.google.android.gms.location.LocationListener;
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

    private long locationRequestInterval = 10000;
    private long fastestLocationRequestInterval = 5000;

    private View markerView = null;
    private Map<Marker, MapModel> markerMap;
    private boolean isLocationRequestPossible, showMarkerWindow = false;
    private
    @DrawableRes
    int userIcon;
    private GoogleApiClient googleApiClient;

    private List<OnGoogleApiConnectedCallback> googleApiConnectedCallbacks;
    private List<PositionCangedListener> positionCangedListeners;

    private boolean isGoogleApiClientReady;

    public MapManagerImpl(Context context) {
        this.context = context;
        markerMap = new HashMap<>();
        googleApiConnectedCallbacks = new ArrayList<>();
        positionCangedListeners = new ArrayList<>();

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
        map.setInfoWindowAdapter(new MarkerInfoWindowAdapter());

    }

    @Override
    public void setMyLocationEnabled(boolean isLocationEnabled) throws LocationRequestNotEnabledException {
        if (map == null)
            return;
        if (!isLocationRequestPossible)
            throw new LocationRequestNotEnabledException();
        else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
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
        if (isGoogleApiClientReady()) {
            addMarkersOnReady(showonMapMandatory, showWithMyPosition, icon, mapModels);
        } else {
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

    protected <T extends MapModel> void addMarkersOnReady(boolean showonMapMandatory,
                                                          boolean showWithMyPosition, @DrawableRes int icon,
                                                          T... mapModels) {
        LatLngBounds.Builder builder = null;
        builder = new LatLngBounds.Builder();
        if (showWithMyPosition && userLatlng != null) {
            builder.include(userLatlng);
        }

        for (T m : mapModels) {
            LatLng ll = new LatLng(m.getLatitude(), m.getLongitude());
            if (showonMapMandatory) {
                builder.include(ll);
            }
            Marker mo = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .icon(BitmapDescriptorFactory.fromResource(icon))
                    .title(getMarkerTitle(m))
                    .snippet(getMarkerSnippet(m))
                    .visible(getMarkerVisibility(m))
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

    protected <T extends MapModel> String getMarkerTitle(T mapModel) {
        return null;
    }

    protected <T extends MapModel> String getMarkerSnippet(T mapModel) {
        return null;
    }

    protected <T extends MapModel> boolean getMarkerVisibility(T mapModel) {
        return true;
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
    public void addPositionChangedListeners(PositionCangedListener positionCangedListener) {
        positionCangedListeners.add(positionCangedListener);
    }

    @Override
    public void clearPositionChangedListeners(PositionCangedListener positionCangedListener) {
        positionCangedListeners.remove(positionCangedListener);
    }

    @Override
    public void clearAllPositionChangedListeners() {
        positionCangedListeners.clear();
    }

    @Override
    public void setIntervalforLocationRequest(long interval) {
        locationRequestInterval = interval;
    }

    @Override
    public void setFastestIntervalforLocationRequest(long interval) {
        fastestLocationRequestInterval = interval;
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
        gotoLocation(userLatlng, isAnimate, zoom);
    }

    @Override
    public void gotoLocation(MapModel mapModel, boolean isAnimate, float zoom) {
        gotoLocation(new LatLng(mapModel.getLatitude(), mapModel.getLongitude()), isAnimate, zoom);
    }

    //set 0 as zoom level if not required
    protected void gotoLocation(LatLng latLng, boolean isAnimate, float zoom) {
        CameraUpdate cu = zoom > 0 ? CameraUpdateFactory.newLatLngZoom(latLng, zoom) : CameraUpdateFactory.newLatLng(latLng);
        if (isAnimate) {
            map.animateCamera(cu);
        } else
            map.moveCamera(cu);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
        googleApiConnectedCallbacks.clear();
        userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        userLatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        if (googleApiConnectedCallbacks != null && !googleApiConnectedCallbacks.isEmpty()) {
            for (OnGoogleApiConnectedCallback cb : googleApiConnectedCallbacks) {
                cb.onConnected(bundle);
            }
            googleApiConnectedCallbacks.clear();


        }

    }
    protected void onLocationUpdated(Location location){
        userLocation = location;
        for (PositionCangedListener pcl: positionCangedListeners) {
            pcl.onPositionChanged();

        }
    }
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationRequestInterval);
        mLocationRequest.setFastestInterval(fastestLocationRequestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, createLocationRequest(), new LocationChangeListener());
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
//call this before setting your locattion or calling other api
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
    /*
    *
Returns

    true if the listener has consumed the event (i.e., the default behavior should not occur);
     false otherwise (i.e., the default behavior should occur).
     The default behavior is for the camera to move to the marker and an info window to appear.

*/
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

    private class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private class LocationChangeListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            onLocationUpdated(location);
        }
    }
}
