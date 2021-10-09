package com.garbagecollection.viewUI

class GarbageCollectionPostData {
    var collectionType: String? = null
    var id: Int? = null
    var noOfBagsCollected: Int? = null
    var notes: String? = null
    var type: String? = null
}

class GarbageCollectionPostDataNew(
    var collectionType: String? = null,
    var id: Int? = null,
    var noOfBagsCollected: Int? = null,
    var notes: String? = null,
    var type: String? = null,
    var selectedDay: String? = null
)