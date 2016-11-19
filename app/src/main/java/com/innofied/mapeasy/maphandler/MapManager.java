package com.innofied.mapeasy.maphandler;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.innofied.mapeasy.maphandler.exceptions.LocationRequestNotEnabledException;

/**
 * Created by debanjan on 31/10/16.
 */

public interface MapManager {
    @IntDef
    @interface PathMode{
        int CAR = 0x11;
        int WALK = 0x22;
    }
    void setPaddingToMap(int left, int right, int top, int bottom);
    void makeMapready(GoogleMap map);
    void setMyLocationEnabled (boolean isLocationEnabled) throws LocationRequestNotEnabledException;
    void addMarkers(boolean showMandatory,boolean showWithMyPosition,@DrawableRes int icon, MapModel... mapModels);
    void clearMarkers();
    void setUserIcon(@DrawableRes int icon);
    void setMarkerWindow(View view);
    void showMarkerWindow(boolean show);
    void setMarkerWindowClickListener(MarkerWindowClickedListener markerClickListener);
    interface MarkerWindowClickedListener {
       void onMarkerWindowClicked (Marker marker);
       void onMarkerWindowLongClicked (Marker marker);
    }
    //first one treated as begin and last one as destination
    void drawPathBetween(@PathMode int mode, MapModel... mapModels);
    void drawPathBetween(@PathMode int mode, MapModel start, MapModel end, MapModel[] waypoints);
    void gotoMyLocation(boolean isAnimate);
    void gotoLocation(MapModel mapModel, boolean isAnimate);
    void gotoMyLocation(boolean isAnimate, float zoom);
    void gotoLocation(MapModel mapModel, boolean isAnimate, float zoom);
}
