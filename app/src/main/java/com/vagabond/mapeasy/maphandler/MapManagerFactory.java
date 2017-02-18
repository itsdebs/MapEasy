package com.vagabond.mapeasy.maphandler;

import android.content.Context;

/**
 * Created by debanjan on 8/11/16.
 */
//extend this class and override create to get your custom mapManagerimpl.
public class MapManagerFactory {
//    private MapManagerFactory() {
//    }

    public <T extends MapManager> T create(T mapManager, Context context){
        return (T) new MapManagerImpl(context);
    }
}
