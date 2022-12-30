package com.example.practicaldemo.db

import com.example.practicaldemo.model.AddressesList


class DataRepository(private val locationDao: LocationDao) {

     fun insert(item: AddressesList) {
         locationDao.insertAll(item)
     }

    fun getAllLocationList(): List <AddressesList> {
        return locationDao.getAllLocationList()
    }

    fun deleteAddress(id :Long) {
        return locationDao.deleteAddress(id)
    }

    fun updateAllUserList(name : String,email : String,image : String,thumbImage : String,id :Long) {
        return locationDao.updateAddress(name,email,image,thumbImage,id)
    }

}