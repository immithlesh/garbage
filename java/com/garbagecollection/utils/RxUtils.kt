package com.garbagecollection.utils

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("CheckResult")
fun subscribeOnBackground(function: () -> Unit) {
    Single.fromCallable {
        function()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({},{ throwable -> Log.d(TAG, "Throwable " + throwable.message) } )
}