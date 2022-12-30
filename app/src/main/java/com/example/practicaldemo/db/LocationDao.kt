package com.example.practicaldemo.db

import androidx.room.*
import com.example.practicaldemo.model.AddressesList

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: AddressesList)

    @Query("SELECT * FROM addresstable")
    fun getAllLocationList(): List <AddressesList>


    @Query("DELETE FROM addresstable WHERE id = :id")
    fun deleteAddress(id: Long)


    @Query("UPDATE addresstable SET latitude=:latitude,longitude=:longitude,cityName=:cityName,address=:address WHERE addresstable.id == :id")
    fun updateAddress(latitude : String , longitude : String , cityName : String ,address : String ,id : Long)

}