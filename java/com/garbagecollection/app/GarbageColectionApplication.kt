package com.garbagecollection.app

import android.app.Application
/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */
class GarbageColectionApplication: Application() {
    companion object {
        var appContext: Application? = null

    }

    override fun onCreate() {
        super.onCreate()
        appContext = this

    }

}