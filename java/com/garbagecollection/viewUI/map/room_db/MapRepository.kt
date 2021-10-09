package com.garbagecollection.viewUI.map.room_db

import android.app.Application
import androidx.lifecycle.LiveData
import com.garbagecollection.network.MapDao
import com.garbagecollection.network.MapDatabase
import com.garbagecollection.utils.subscribeOnBackground

class MapRepository(application: Application) {

    var mapDao: MapDao
    private val database = MapDatabase.getUserTableInstance(application)

    init {
        mapDao = database.mapDao()
    }

    fun insertMapDriver(mapDriver: GCDriverModel) {
        subscribeOnBackground {
            mapDao.insertMapDriver(mapDriver)
        }
    }

    fun exixtingMapDriver(Uid: Int,day: String): LiveData<GCDriverModel> {
        return mapDao.getMapExistingDriver(Uid,day)
    }

    fun updateMapDriver(mapDriver: GCDriverModel) {
        subscribeOnBackground {
            mapDao.updateMapDriver(mapDriver)
        }
    }
    fun getAllMapDriver(id: Int, day: String): LiveData<GCDriverModel> {
        return mapDao.getAllMapDriver(id, day)
    }

    fun getFinalMapData(id: Int, day: String, direction: String,detailType: String): LiveData<GCDriverModel> {
        return mapDao.getFinalMapData(id, day, direction,detailType)
    }

}
