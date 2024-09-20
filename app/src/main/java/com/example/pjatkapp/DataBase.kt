package com.example.pjatkapp

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class DataBase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}