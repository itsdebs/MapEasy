package com.vagabond.mapeasy.maphandler;

import android.content.Context;

/**
 * Created by debanjan on 8/11/16.
 */
public class MapManagerFactory {
//    private MapManagerFactory() {
//    }

    public <T extends MapManager> T create( Context context){
        return MapManagerImpl.getInstance(context);
    }
}
