package com.personalbiography.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EntryEntity::class, UsageEventEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BiographyDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    abstract fun usageDao(): UsageDao

    companion object {
        fun build(context: Context): BiographyDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                BiographyDatabase::class.java,
                "biography.db",
            ).build()
    }
}
