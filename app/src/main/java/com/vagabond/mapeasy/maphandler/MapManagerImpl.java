package com.vagabond.mapeasy.maphandler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.vagabond.mapeasy.maphandler.exceptions.LocationRequestNotEnabledException;
import com.vagabond.mapeasy.maphandler.helper.PathToDestination;
import com.vagabond.mapeasy.maphandler.model.MapModel;
import com.vagabond.mapeasy.maphandler.model.MapModelAlti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    private float defaultZoom = 18.0f;
    private float maxZoomLevel = 18.0f;
    private long locationRequestInterval = 10000;
    private long fastestLocationRequestInterval = 10000;

    private Map<Marker, MapModel> markerMap;
    private boolean isLocationRequestPossible, showMarkerWindow = false;
    private
    @DrawableRes
    int userIcon = 0;
    private MyLocation myLocation;
    private GoogleApiClient googleApiClient;

    private CameraPosition prevCameraPosition;

    private List<OnGoogleApiConnectedCallback> googleApiConnectedCallbacks;
    private List<PositionCangedListener> positionCangedListeners;
    private List<CameraPositionChangedListener> cameraPositionChangedListeners;

    private boolean isGoogleApiClientReady;

     protected MapManagerImpl(Context context) {
        this.context = context;
        markerMap = new HashMap<>();
        googleApiConnectedCallbacks = new ArrayList<>();
        positionCangedListeners = new ArrayList<>();
        cameraPositionChangedListeners = new ArrayList<>();


    }
    static <T extends MapManager>  T  getInstance(Context context){
        return new MapManagerImpl(context).getMe(context);
    }
    protected <T extends MapManager> T getMe(Context context){
        return (T) new MapManagerImpl(context);
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
        map.setOnCameraMoveListener(new CameraMoveListener());
        map.setOnCameraIdleListener(new CameraIdleListener());
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

    @Override
    public synchronized <T extends MapModel>  void  removeMarkers(Collection<T> mapModels) {
        Iterator<Map.Entry<Marker, MapModel>> it = markerMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Marker, MapModel> entry = it.next();
            try {
                if ( mapModels.contains(entry.getValue())) {
                    entry.getKey().remove();
//                markerMap.remove(entry.getKey());
                    mapModels.remove(entry.getValue());
                    it.remove();

                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public synchronized boolean moveMarker(MapModel mapModel, double lat, double lng) {
        Iterator<Map.Entry<Marker, MapModel>> it = markerMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Marker, MapModel> entry = it.next();
            if(mapModel.equals(entry.getValue())){
                entry.getKey().setPosition(new LatLng(lat,lng));
                entry.setValue(mapModel);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean rotateMarker(MapModel mapModel, float degs) {

        Iterator<Map.Entry<Marker, MapModel>> it = markerMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Marker, MapModel> entry = it.next();
            if(mapModel.equals(entry.getValue())){
                entry.getKey().setRotation(degs);
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    protected synchronized final <T extends MapModel> void addMarkersOnReady(boolean showonMapMandatory,
                                                                             boolean showWithMyPosition, @DrawableRes int icon,
                                                                             T... mapModels) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
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
                    .flat(true)
            );

            markerMap.put(mo, m);
        }
        /*if (userIcon <= 0) {
            map.addMarker(new MarkerOptions()
                    .position(userLatlng).icon(BitmapDescriptorFactory.fromResource(userIcon)));
        }*/
        if ((showonMapMandatory || showWithMyPosition) && mapModels.length > 0) {
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(false);
    }

    @Override
    public <T extends MapModel> void setMarkerWindow(View view, T mapModel) {

    }



    @Override
    public void showMarkerWindow(boolean show) {
        showMarkerWindow = show;
    }

    @Override
    public void setMarkerWindowClickListener(MarkerWindowClickedListener markerWindowClickedListener) {

    }

    @Nullable
    @Override
    public MapModel getCurrenrUserPos() {
        if (userLocation == null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return null;
            }
            userLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
        }
        return userLocation == null? null: new MapModel() {
            @Override
            public double getLatitude() {
                return userLocation.getLatitude();
            }

            @Override
            public double getLongitude() {
                return userLocation.getLongitude();
            }
        };
    }

    @Override
    public void addCameraPositionChangedListeners(CameraPositionChangedListener cameraPositionChangedListener) {
        cameraPositionChangedListeners.add(cameraPositionChangedListener);
    }

    @Override
    public void clearCameraPositionChangedListeners(CameraPositionChangedListener cameraPositionChangedListener) {
        cameraPositionChangedListeners.remove(cameraPositionChangedListener);
    }

    @Override
    public void clearAllCameraPositionChangedListeners() {
        cameraPositionChangedListeners.clear();
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
    public void drawPathBetween(@PathMode int mode, @Nullable String pathColor, MapModel... mapModels) {
        drawPathBetween(mode, pathColor, -1, mapModels);
    }

    @Override
    public void drawPathBetween(@PathMode int mode, MapModel start, @Nullable String pathColor,
                                MapModel end, MapModel[] waypoints) {
        drawPathBetween(mode, start, pathColor, -1, end, waypoints);
    }

    @Override
    public void drawPathBetween(@PathMode int mode, @Nullable String pathColor, int pathWidth, MapModel... mapModels) {
        if (mapModels.length < 2)
            return;
        MapModel start = mapModels[0];
        MapModel end = mapModels[mapModels.length - 1];
        drawPathBetween(mode, start, pathColor, pathWidth, end, Arrays.copyOfRange(mapModels, 1, mapModels.length));

    }

    @Override
    public void drawPathBetween(@PathMode int mode, MapModel start, @Nullable String pathColor, int pathWidth,
                                MapModel end, MapModel[] waypoints) {
       PathToDestination pathToDestination = new PathToDestination(context, map);

        if (pathToDestination == null)
            return;
        if (pathColor != null) {
            pathToDestination.setPathColor(pathColor);
        }
        if (pathWidth > 0) {
            pathToDestination.setPathWidth(pathWidth);
        }
        pathToDestination.drawPathBetween(mode, start.getLatitude(), start.getLongitude(), end.getLatitude(),
                end.getLongitude(), waypoints);
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
        if (latLng != null) {
            CameraUpdate cu = zoom > 0 ? CameraUpdateFactory.newLatLngZoom(latLng, zoom) : CameraUpdateFactory.newLatLng(latLng);
            if (isAnimate) {
                map.animateCamera(cu);
            } else {
                map.moveCamera(cu);
            }
//            onCameraMoved();
        }
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
        /*userLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (userLocation != null)
            userLatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());*/
        isGoogleApiClientReady = true;
        if (googleApiConnectedCallbacks != null && !googleApiConnectedCallbacks.isEmpty()) {
            for (OnGoogleApiConnectedCallback cb : googleApiConnectedCallbacks) {
                cb.onConnected(bundle);
            }
            googleApiConnectedCallbacks.clear();


        }

    }

    protected void onLocationUpdated(Location location) {
        userLocation = location;
        if (myLocation == null) {
            myLocation = new MyLocation();
        }
        if (userLocation != null) {
            userLatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            myLocation.latitude = userLocation.getLatitude();
            myLocation.longitude = userLocation.getLongitude();
        }
        if (userIcon > 0) {
            if (myLocation != null) {
                if (!moveMarker(myLocation, userLocation.getLatitude(), userLocation.getLongitude()))
                    addMarkers(false, false, userIcon, myLocation);
            }

        }

        for (int i = 0; i < positionCangedListeners.size(); i++) {
            PositionCangedListener pcl = positionCangedListeners.get(i);
            pcl.onPositionChanged(new MapModelAlti() {
                @Override
                public double getAltitude() {
                    return userLocation.getAltitude();
                }

                @Override
                public double getLatitude() {
                    return userLocation.getLatitude();
                }

                @Override
                public double getLongitude() {
                    return userLocation.getLongitude();
                }
            });

        }
    }
    @Override
    public void stopLocationUpdates() {
        if(googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, locationChangeListener);
        }
    }
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationRequestInterval);
        mLocationRequest.setFastestInterval(fastestLocationRequestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
    private LocationChangeListener locationChangeListener = new LocationChangeListener();
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, createLocationRequest(), locationChangeListener);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        isGoogleApiClientReady = false;
        if (googleApiConnectedCallbacks != null && !googleApiConnectedCallbacks.isEmpty()) {
            for (OnGoogleApiConnectedCallback cb : googleApiConnectedCallbacks) {
                cb.onSuspended(cause);
            }
            googleApiConnectedCallbacks.clear();
        }
    }

    //call this before setting your locattion or calling other api
    @Override
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
    public void googleAPIConnect() {
        googleApiClient.connect();
    }

    @Override
    public void showMyLocationOnMap() {
        //map.clear();
        // Setting position for the marker
        if (userLatlng == null) {
            PositionCangedListener pos = new PositionCangedListener() {
                @Override
                public void onPositionChanged(MapModel mapModel) {
                    if (userLatlng != null) {
                        gotoMyLocation(false, defaultZoom);
                    }
                    positionCangedListeners.remove(this);
                }
            };
            addPositionChangedListeners(pos);
        } else {
            gotoMyLocation(false, 19.0f);
        }
    }

    @Override
    public double[] getMyLocation() {
        return new double[]{userLatlng.latitude, userLatlng.longitude};
    }

    @Override
    public MapModel getMapCenter() {
        Projection projection = map.getProjection();
        VisibleRegion visibleRegion = projection
                .getVisibleRegion();

        Point x = projection.toScreenLocation(
                visibleRegion.farRight);

        Point y = projection.toScreenLocation(
                visibleRegion.nearLeft);

        Point centerPoint = new Point(x.x / 2, y.y / 2);

        final LatLng centerFromPoint = projection.fromScreenLocation(
                centerPoint);
        return new MapModel() {
            @Override
            public double getLatitude() {
                return centerFromPoint.latitude;
            }

            @Override
            public double getLongitude() {
                return centerFromPoint.longitude;
            }
        };
    }

    @Override
    public void includePointsInVisibleMap(boolean includeMe, MapModel... mapModels) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (includeMe && userLatlng != null) {
            builder.include(userLatlng);
        }

        for (MapModel m : mapModels) {
            LatLng ll = new LatLng(m.getLatitude(), m.getLongitude());
            builder.include(ll);
        }
        /*if (userIcon <= 0) {
            map.addMarker(new MarkerOptions()
                    .position(userLatlng).icon(BitmapDescriptorFactory.fromResource(userIcon)));
        }*/
        if (includeMe || mapModels.length > 0) {
            LatLngBounds latLngBounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (60
                    * context.getResources().getDisplayMetrics().density));

            moveOrAnimateCamera(cu);
        }
    }

    private void addMarker(LatLng userLatlng, int drawable) {
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(userLatlng);
        // Setting custom icon for the marker
        markerOptions.icon(BitmapDescriptorFactory.fromResource(drawable));
        // Adding the marker to the map
        map.addMarker(markerOptions);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected interface OnGoogleApiConnectedCallback {
        void onConnected(@Nullable Bundle bundle);

        void onSuspended(int cause);
    }

    public interface ActivityCallback {
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
    protected boolean onMarkerClickCompleted() {
        return false;
    }

    protected boolean onMarkerClicked(Marker marker) {
        return onMarkerClicked(marker, markerMap.get(marker));
    }

    private void onCameraMoved() {
        onCameraMoved(map.getCameraPosition());
    }

    protected void onCameraMoved(CameraPosition cameraPosition) {
        if (cameraPositionChangedListeners != null) {
            double dist = 0;
            double prevLat = -1;
            double prevLng = -1;
            double newLat = cameraPosition.target.latitude;
            double newLng = cameraPosition.target.longitude;
            if (prevCameraPosition != null) {
                prevLat = prevCameraPosition.target.latitude;
                prevLng = prevCameraPosition.target.longitude;
                dist = distance(prevLat, prevLng, newLat
                        , newLng);
            }
            prevCameraPosition = cameraPosition;
            for (CameraPositionChangedListener cpl : cameraPositionChangedListeners) {
                cpl.onCameraPositionChanged(prevLat, prevLng, newLat, newLng, dist);
            }
        }

    }
    //metres
    @Override
    public double distance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }

    protected boolean onMarkerClicked(Marker marker, MapModel mapModel) {
        gotoLocation(mapModel, true);
        return onMarkerClickCompleted();
    }

    private class MarkerClickListener implements GoogleMap.OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {

            return onMarkerClick(marker);
        }
    }

    private class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private class LocationChangeListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            onLocationUpdated(location);
        }
    }

    private class CameraMoveListener implements GoogleMap.OnCameraMoveListener {

        @Override
        public void onCameraMove() {
        }
    }

    private class CameraIdleListener implements GoogleMap.OnCameraIdleListener {

        @Override
        public void onCameraIdle() {
            onCameraMoved();
        }
    }

    // only one instance to be created
    private class MyLocation implements MapModel {
        double latitude, longitude;

        @Override
        public double getLatitude() {
            return latitude;
        }

        @Override
        public double getLongitude() {
            return longitude;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MyLocation;
        }
    }
}
