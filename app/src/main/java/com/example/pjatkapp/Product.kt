package com.example.pjatkapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")

data class Product(

    var category: String,
    var state: String,
    var photo: ByteArray?,
    var name: String,
    var expirationDate: String,
    var quantity: String?,
    var isDisposed: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
