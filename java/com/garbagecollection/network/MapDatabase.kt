package com.garbagecollection.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.garbagecollection.utils.subscribeOnBackground
import com.garbagecollection.viewUI.map.room_db.GCDriverModel


@Database(entities = [GCDriverModel::class], version =8, exportSchema = true)
//@TypeConverters(ConverterClass::class)
abstract class MapDatabase : RoomDatabase() {

    abstract fun mapDao(): MapDao

    companion object {
        private var tableInstance: MapDatabase? = null

        @Synchronized
        fun getUserTableInstance(ctx: Context): MapDatabase {
            if(tableInstance == null)
                tableInstance = Room.databaseBuilder(ctx.applicationContext, MapDatabase::class.java,
                    "GarbageCollectionDB")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return tableInstance!!

        }
        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                populateDatabase(tableInstance!!)
            }
        }
        private fun populateDatabase(db: MapDatabase) {
            val mapDao = db.mapDao()
            subscribeOnBackground {
            }
        }
    }



}