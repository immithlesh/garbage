package com.garbagecollection.viewUI.map.room_db

import android.app.Application
import androidx.lifecycle.*
import com.garbagecollection.ContainerActivity
import com.garbagecollection.network.MapDatabase


class MapViewModel(val app: Application) : AndroidViewModel(app) {

    var getupdateListener = MutableLiveData<Boolean>()
    var gcwebDriverData: GCDriverModel? = null

    private val repository = MapRepository(app)

    fun insertMapDriver(mapDriver: GCDriverModel) {
        repository.insertMapDriver(mapDriver)
    }

    fun existingMapDriver(
        id: Int,
        day: String,
        containerActivity: ContainerActivity
    ): LiveData<GCDriverModel> {
        return repository.exixtingMapDriver(id, day)
    }

    fun getExistingDriver(
        id: Int,
        direction: String,
        day: String, detailType: String,
        containerActivity: ContainerActivity
    ) {
        val existingDriver: GCDriverModel = MapDatabase.getUserTableInstance(app).mapDao().getExistingDriver(id, direction, day, detailType)
        gcwebDriverData = existingDriver
        getupdateListener.postValue(true)
    }

    fun updateRoute(
        directionResponse: String,
        dayResponse: String,
        day: String,
        detailType: String,
        id: Int
    ) {
        MapDatabase.getUserTableInstance(app).mapDao()
            .updateRoute(directionResponse, dayResponse, day, detailType, id)
    }

    fun updateMapDriver(mapDriver: GCDriverModel) {
        return repository.updateMapDriver(mapDriver)
    }

    fun getAllMapDriver(id: Int, day: String): LiveData<GCDriverModel> {
        return repository.getAllMapDriver(id, day)
    }

    fun deleteUser(id: Int) {
        MapDatabase.getUserTableInstance(app).mapDao().deleteUser(id)
    }

    fun updateRoute2(beckendResponse: String,polyLineResponse:String, id: Int) {
        MapDatabase.getUserTableInstance(app).mapDao().updateRoute2(beckendResponse,polyLineResponse, id)
    }

    fun getFinalMapData(
        id: Int,
        day: String,
        direction: String,
        detailType: String
    ): LiveData<GCDriverModel> {
        return repository.getFinalMapData(id, day, direction, detailType)
    }
}