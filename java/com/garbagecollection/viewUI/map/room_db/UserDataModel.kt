package com.garbagecollection.viewUI.map.room_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Driver_table")

data class GCDriverModel(
    val driverId:Int,
    val direction: String?=null,
    val directionResponse:String?=null,
    val day:String?=null,
    val dayResponse:String?=null,
    val polylineResponse:String?=null,
    val backendResponse:String?=null,
    val detailType:String?=null,
    val name:String?=null,
    val location:String?=null,
    val notes:String?=null,
    val pinColor: String?=null,
    @PrimaryKey(autoGenerate = true) val id: Int?=null)

@Entity(tableName = "Driver_table")
data class GCDriverModel1(
    val driverId:Int,
    val direction: String?=null,
    val directionResponse:String?=null,
    val day:String?=null,
    val dayResponse:String?=null,
    val polylineResponse:String?=null,
    val backendResponse:String?=null,
    val detailType:String?=null,
    val name:String?=null,
    val location:String?=null,
    val notes:ArrayList<Note>?=null,
    val pinColor: String?=null,
    @PrimaryKey(autoGenerate = true) val id: Int?=null)

data class Note(
    val createdDate: String?,
    val id: Int?,
    val notes: String?
)

