package com.example.practicaldemo.view

import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicaldemo.adapter.AddressListAdapter
import com.example.practicaldemo.model.AddressesList
import com.example.practicaldemo.R
import com.example.practicaldemo.utils.Utils
import com.example.practicaldemo.viewmodel.DataViewModel
import kotlinx.android.synthetic.main.activity_location_source.*
import java.util.*
import kotlin.collections.ArrayList

class LocationSourceActivity : AppCompatActivity() {

    private var addressList: ArrayList<AddressesList> = ArrayList()
    private var addressListAdapter: AddressListAdapter? = null
    private lateinit var dataRecordViewModel: DataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_source)
        supportActionBar!!.title =getString(R.string.location_source)

        dataRecordViewModel = ViewModelProvider(this).get(DataViewModel::class.java)

        addressList.addAll(dataRecordViewModel.getAllLocationList())

        rvAddressList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        addressListAdapter = AddressListAdapter(addressList,dataRecordViewModel)
        rvAddressList.adapter = addressListAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.ascending -> {
                sortByDistance(true, Utils.currentLatitude, Utils.currentLongitude)
            }
            R.id.descending -> {
                sortByDistance(false, Utils.currentLatitude, Utils.currentLongitude)
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun sortByDistance(
        isAscending : Boolean,
        myLatitude: Double,
        myLongitude: Double
    ){
        val comp = Comparator<AddressesList?> { o, o2 ->
            val result1 = FloatArray(3)
            Location.distanceBetween(myLatitude, myLongitude, o.latitude.toDouble(), o.longitude.toDouble(), result1)
            val distance1 = result1[0]
            val result2 = FloatArray(3)
            Location.distanceBetween(myLatitude, myLongitude, o2.latitude.toDouble(), o2.longitude.toDouble(), result2)
            val distance2 = result2[0]
            distance1.compareTo(distance2)
        }
        Collections.sort(addressList, comp)
        if (!isAscending){
            addressList.reverse()
        }
        addressListAdapter!!.notifyDataSetChanged()

    }
}