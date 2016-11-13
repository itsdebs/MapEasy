package com.innofied.mapeasy.maphandler;

/**
 * Created by debanjan on 8/11/16.
 */

public class MapManagerFactory {
//    private MapManagerFactory() {
//    }

    public <T extends MapManager, Q extends T> T create(T mapManager, Q mapManagerImpl){
        return mapManagerImpl;
    }

}
