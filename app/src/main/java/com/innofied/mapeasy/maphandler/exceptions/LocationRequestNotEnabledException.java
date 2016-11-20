package com.innofied.mapeasy.maphandler.exceptions;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by debanjan on 13/11/16.
 */

public class LocationRequestNotEnabledException extends Exception {
    public LocationRequestNotEnabledException() {
        this("Need to call createLocationRequest Method first, on callback call this method");
    }

    public LocationRequestNotEnabledException(String message) {
        super(message);
    }

    public LocationRequestNotEnabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocationRequestNotEnabledException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LocationRequestNotEnabledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
