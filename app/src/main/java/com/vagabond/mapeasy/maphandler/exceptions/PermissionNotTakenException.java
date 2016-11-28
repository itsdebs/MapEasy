package com.vagabond.mapeasy.maphandler.exceptions;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by debanjan on 10/11/16.
 */

public class PermissionNotTakenException extends Exception {

    public PermissionNotTakenException() {
        super("Please ensure runtime permissions");
    }

    public PermissionNotTakenException(String message) {
        super(message);
    }

    public PermissionNotTakenException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionNotTakenException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PermissionNotTakenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
