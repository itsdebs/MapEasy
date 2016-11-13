package com.innofied.mapeasy.maphandler.exceptions;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by debanjan on 10/11/16.
 */

public class MapLocationNotEnabledException extends Exception {
    public MapLocationNotEnabledException() {
        super("Turn on my location. map.setMyLocationEnabled(true)");
    }

    public MapLocationNotEnabledException(String message) {
        super(message);
    }

    public MapLocationNotEnabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapLocationNotEnabledException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MapLocationNotEnabledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
