package com.vagabond.mapeasy.maphandler;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.vagabond.mapeasy.maphandler.exceptions.LocationRequestNotEnabledException;
import com.vagabond.mapeasy.maphandler.model.MapModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;

/**
 * Created by debanjan on 31/10/16.
 */

public interface MapManager {
    @IntDef({PathMode.CAR,PathMode.WALK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PathMode {
        int CAR = 0x11;
        int WALK = 0x22;
    }
    /*@IntDef({AuthIdentifier.DEVELOPMENT, AuthIdentifier.PRODUCTION, AuthIdentifier.QA})
    public @interface AuthIdentifier {
        int DEVELOPMENT = 0X11;
        int PRODUCTION = 0X22;
        int QA = 0X33;
    }*/
    void setPaddingToMap(int left, int right, int top, int bottom);

    void makeMapready(GoogleMap map);

    void setMyLocationEnabled(boolean isLocationEnabled) throws LocationRequestNotEnabledException;

    void addMarkers(boolean showMandatory, boolean showWithMyPosition, @DrawableRes int icon, MapModel... mapModels);

    <T extends MapModel> void removeMarkers(Collection<T> mapModels);

    boolean moveMarker(MapModel mapModel, double lat, double lng);

    boolean rotateMarker(MapModel mapModel, float degs);

    void clearMarkers();

    void setUserIcon(@DrawableRes int icon);

    <T extends MapModel> void  setMarkerWindow(View view, T mapModel);

    void showMarkerWindow(boolean show);

    void setMarkerWindowClickListener(MarkerWindowClickedListener markerClickListener);

    interface MarkerWindowClickedListener {
        void onMarkerWindowClicked(Marker marker);

        void onMarkerWindowLongClicked(Marker marker);
    }

    interface CameraPositionChangedListener {
        void onCameraPositionChanged(double prevLat, double prevLng, double latNow, double lngNow, double displacement);
    }
    @Nullable MapModel getCurrenrUserPos();
    void addCameraPositionChangedListeners(CameraPositionChangedListener cameraPositionChangedListener);

    void clearCameraPositionChangedListeners(CameraPositionChangedListener cameraPositionChangedListener);

    void clearAllCameraPositionChangedListeners();

    interface PositionCangedListener<T extends MapModel> {
        //specify the distance in metres you want method onPositionChanged(int metres) to be called.
        // onPositionChanged() will be called everytime position changes
        //return 0 if not required
//        float getCallbackDistance();
        void  onPositionChanged(T mapModel);
        //whenever the position changes by or more than the specified distance,from the point where the method was last called,
        // this is to be called
        //not called if callback distance is 0

//        void onPositionChanged(float metres);
    }


    //add position changed listener

    void addPositionChangedListeners(PositionCangedListener positionCangedListener);

    void clearPositionChangedListeners(PositionCangedListener positionCangedListener);

    void clearAllPositionChangedListeners();

    void setIntervalforLocationRequest(long interval);

    void setFastestIntervalforLocationRequest(long interval);

    //first one treated as begin and last one as destination
    void drawPathBetween(@PathMode int mode, @Nullable String pathColor, MapModel... mapModels);

    void drawPathBetween(@PathMode int mode, MapModel start, @Nullable String pathColor, MapModel end, MapModel[] waypoints);

    void drawPathBetween(@PathMode int mode, @Nullable String pathColor, int pathWidth, MapModel... mapModels);

    void drawPathBetween(@PathMode int mode, MapModel start, @Nullable String pathColor, int pathWidth,
                         MapModel end, MapModel[] waypoints);

    //hexadecimal code. Default is google map blue.
    void gotoMyLocation(boolean isAnimate);

    void gotoLocation(MapModel mapModel, boolean isAnimate);

    void gotoMyLocation(boolean isAnimate, float zoom);

    void gotoLocation(MapModel mapModel, boolean isAnimate, float zoom);

    void createLocationRequest(MapManagerImpl.ActivityCallback activityCallback);
    public double distance(double lat_a, double lng_a, double lat_b, double lng_b);

    void googleAPIConnect();

    void showMyLocationOnMap();

    double[] getMyLocation();

    MapModel getMapCenter();

    void includePointsInVisibleMap(boolean includeMe, MapModel... mapModels);
    void stopLocationUpdates();
}
