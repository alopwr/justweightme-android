package io.github.gladko.justweight.ui.btscan

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.gladko.justweight.databinding.ListItemBtDeviceBinding

interface OnClickListener {
    fun onConnectMeClick(macAddress: String)
}
class BtScanAdapter(private val onClickListener: OnClickListener) : ListAdapter<BtScanObject, BtScanAdapter.ViewHolder>(BTScanDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBtDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val btDevice = getItem(position)
        holder.apply {
            bind(createOnClickListener(btDevice.address), btDevice)
        }
    }

    private fun createOnClickListener(mac:String): View.OnClickListener {
        return View.OnClickListener {
            onClickListener.onConnectMeClick(mac)
        }
    }

    class ViewHolder(
        private val binding: ListItemBtDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, item: BtScanObject) {
            binding.apply {
                clickListener = listener
                btDevice = item
                executePendingBindings()
            }
        }
    }


    private class BTScanDiffCallback : DiffUtil.ItemCallback<BtScanObject>() {
        override fun areItemsTheSame(oldItem: BtScanObject, newItem: BtScanObject): Boolean {
            return oldItem.address == oldItem.address
        }

        override fun areContentsTheSame(
            oldItem: BtScanObject,
            newItem: BtScanObject
        ): Boolean {
            return oldItem.address == newItem.address
        }


    }



}
