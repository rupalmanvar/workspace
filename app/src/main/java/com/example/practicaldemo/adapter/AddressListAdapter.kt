package com.example.practicaldemo.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practicaldemo.R
import com.example.practicaldemo.viewmodel.DataViewModel
import com.example.practicaldemo.model.AddressesList
import com.example.practicaldemo.view.LocationSearchActivity
import kotlinx.android.synthetic.main.address_list_item.view.*
import java.io.Serializable


class AddressListAdapter(
    var addressList: ArrayList<AddressesList>,
    var dataRecordViewModel: DataViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mContext: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        val layoutInflater =
            LayoutInflater.from(mContext)
        val listItem: View = layoutInflater.inflate(
            R.layout.address_list_item,
            parent,
            false
        )
        return ViewHolder(listItem)
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {


        val address = addressList[position]
        holder.itemView.txtAddress.text = address.address
        holder.itemView.txtType.text = address.cityName

        holder.itemView.btnEditAddress.setOnClickListener {
            openLocationSearchScreen(position, true)
        }

        holder.itemView.btnDeleteAddress.setOnClickListener {
            dataRecordViewModel.deleteAddress(addressList[position].id)
            addressList.removeAt(position)
            notifyDataSetChanged()
        }

        holder.itemView.setOnClickListener {
            openLocationSearchScreen(position, false)
        }

    }


    override fun getItemCount(): Int {
        return addressList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    private fun openLocationSearchScreen(position: Int, isEdit: Boolean) {
        val address = addressList[position]
        val bundle = Bundle()
        bundle.putSerializable("AddressList", addressList as Serializable)
        bundle.putLong("AddressId", address.id)
        bundle.putBoolean("isEdit", isEdit)
        var intent = Intent(mContext, LocationSearchActivity::class.java)
        intent.putExtras(bundle)
        mContext!!.startActivity(intent)
    }


}