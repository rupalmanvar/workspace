package com.example.practicaldemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practicaldemo.db.AppDatabase
import com.example.practicaldemo.db.DataRepository
import com.example.practicaldemo.model.AddressesList
import kotlinx.coroutines.launch

class DataViewModel(application: Application) : AndroidViewModel(application)
{
    private val userRepository: DataRepository

    init {
        val dao = AppDatabase.buildDatabase(application).locationDao()
        userRepository = DataRepository(dao)
    }

    fun insert(item: AddressesList) = viewModelScope.launch {
        userRepository.insert(item)
    }

    fun updateData(latitude : String,longitude : String,cityName : String,address : String,id :Long) {
        userRepository.updateAllUserList(latitude ,longitude,cityName,address,id)
    }
    fun deleteAddress(id :Long) {
        userRepository.deleteAddress(id)
    }

   fun getAllLocationList() = userRepository.getAllLocationList()


}