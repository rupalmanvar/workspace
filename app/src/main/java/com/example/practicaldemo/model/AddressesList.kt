package com.example.practicaldemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity(tableName = "addresstable")
data class AddressesList(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var latitude: String,
    var longitude: String,
    var cityName: String,
    var address: String
) : Serializable
