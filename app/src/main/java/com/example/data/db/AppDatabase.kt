package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.InspectionDao
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity

@Database(
    entities = [
        BuildingEntity::class,
        ElementEntity::class,
        FloorInspectionEntity::class,
        FloorObservationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspectionDao(): InspectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_site_inspect.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
