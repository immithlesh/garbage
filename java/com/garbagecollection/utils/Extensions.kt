package com.garbagecollection.utils

import android.widget.EditText


val  EditText.content: String
    get(){
        return this.text.trim().toString()
    }


