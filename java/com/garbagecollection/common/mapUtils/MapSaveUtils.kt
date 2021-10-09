package com.garbagecollection.common.mapUtils

data class InfoWindowTags(val selectedPos : Int=-1,val notes:String="",val tag : MarketTagsUtils = MarketTagsUtils.none )

data class MarkersInfo(val id:Int,val data:ArrayList<InfoWindowTags>)

data class CustomSoredMarkers(val data:ArrayList<MarkersInfo>)