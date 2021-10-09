package com.garbagecollection.network

import androidx.lifecycle.LiveData
import androidx.room.*
import com.garbagecollection.viewUI.map.room_db.GCDriverModel


@Dao
interface MapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMapDriver(mapRouteUser: GCDriverModel)

    @Update
    fun updateMapDriver(mapRouteUser: GCDriverModel)

    @Query("select * from Driver_table where driverId=:Uid and day=:day")
    fun getAllMapDriver(Uid: Int, day: String): LiveData<GCDriverModel>

    @Query("select * from Driver_table where driverId=:Uid and day=:day and direction=:direction and detailType=:detailType")
    fun getFinalMapData(Uid: Int, day: String,direction: String,detailType: String): LiveData<GCDriverModel>

    @Query("select * from Driver_table where driverId=:Uid and day=:day")
    fun getMapExistingDriver(Uid: Int,day: String): LiveData<GCDriverModel>

    @Query("select * from Driver_table where driverId=:Uid and direction=:direction and day=:day and detailType=:detailType")
    fun getExistingDriver(Uid: Int, direction: String?, day: String?,detailType: String): GCDriverModel

    @Query("UPDATE Driver_table SET directionResponse=:directionResponse , dayResponse=:dayResponse,day=:day,detailType=:detailType WHERE id=:id")
    fun updateRoute(directionResponse:String,dayResponse:String,day: String,detailType: String,id:Int)

    @Query("UPDATE Driver_table SET backendResponse=:backendResponse, polylineResponse=:polyLineResponse  WHERE id=:id")
    fun updateRoute2(backendResponse:String,polyLineResponse:String,id:Int)

    @Query("DELETE FROM Driver_table WHERE id=:id")
    fun deleteUser(id: Int)


   /* @Query("delete from Driver_table where day=:day")
    fun deleteRowByDay(day: String):GCDriverModel
*/
}