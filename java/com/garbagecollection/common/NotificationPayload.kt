package com.garbagecollection.common

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */

class NotificationPayload {
    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("body")
    @Expose
    var body: String? = null

    @SerializedName("tag")
    @Expose
    var tag: String? = null

}

 class Notification {
    val content_available: Any?=null
    val data: Data?=null
    val notification: NotificationX?=null
    val priority: Any?=null
    val registration_ids: List<Any>?=null
    val to: Any?=null
}

 class Data {
    val body: Any?=null
    val notes: String?=null
    val receiverId: Any?=null
    val senderId: Any?=null
    val tag: String?=null
    val targetId: Any?=null
    val title: String?=null
    val type: String?=null
    val user: Any?=null
}

@Keep
 class NotificationX{
    val body: Any?=null
    val notes: String?=null
    val receiverId: Any?=null
    val senderId: Any?=null
    val tag: String?=null
    val targetId: Any?=null
    val title: String?=null
    val type: String?=null
    val user: Any?=null
}