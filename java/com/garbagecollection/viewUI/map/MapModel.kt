package com.garbagecollection.viewUI.map

data class FetchMarkerModel(
    val data: Data?,
    val message: String?
)
data class Data(
    val results: ArrayList<Result>?
)
data class  Result(
    val creationDate: String?,
    val detailType: String?,
    val id: Int?,
    val isHold: Boolean?,
    val latitude: Double?,
    val location: String?,
    val longitude: Double?,
    val name: String?,
    val nextPickUpDate: String?,
    val pickupType: PickupType?,
    val route: Route?,
    var notes: ArrayList<Note>?,
    var pinColor: String?,
    val selectedDay: String?,
    val updatedDate: Any?,
    var isNewKey :Boolean= false,
    var isGarbageSubmit :Boolean= false,
    var isNew :Boolean= false

)
data class PickupType(
    val id: Int?,
    val pickupType: String?
)
data class Route(
    val id: Int?,
    val routeName: String?
)
data class Note(
    val createdDate: String?,
    val id: Int?,
    val notes: String?
)