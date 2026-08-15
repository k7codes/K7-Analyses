package com.example.veritabani

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AnalizKayitEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnalizVeritabani : RoomDatabase() {
    abstract fun analizKayitDao(): AnalizKayitDao

    companion object {
        @Volatile
        private var ORNEK: AnalizVeritabani? = null

        fun veritabaniniGetir(context: Context): AnalizVeritabani {
            return ORNEK ?: synchronized(this) {
                val yeniOrnek = Room.databaseBuilder(
                    context.applicationContext,
                    AnalizVeritabani::class.java,
                    "k7_analiz_veritabani.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                ORNEK = yeniOrnek
                yeniOrnek
            }
        }
    }
}
