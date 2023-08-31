package com.example.chillbeer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BeerEntity::class],
    version = 1
)
abstract class BeerDB : RoomDatabase() {
    abstract val dao: BeerDao
}
