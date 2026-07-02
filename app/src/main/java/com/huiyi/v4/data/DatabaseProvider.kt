package com.huiyi.v4.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var database: HuiyiDatabase? = null

    fun get(context: Context): HuiyiDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                HuiyiDatabase::class.java,
                "huiyi-v4.db"
            ).build().also { database = it }
        }
    }
}
