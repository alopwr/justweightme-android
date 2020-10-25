package io.github.gladko.justweight.ui.main.home



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.gladko.justweight.databinding.ListItemMeasurmentBinding
import io.github.gladko.justweight.db.Measurement


class List1Adapter() : ListAdapter<Measurement, List1Adapter.ViewHolder>(ListAdapterDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemMeasurmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val btDevice = getItem(position)
        holder.apply {
            bind(btDevice)
        }
    }



    class ViewHolder(
        private val binding: ListItemMeasurmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Measurement) {
            binding.apply {
                btDevice = item
                executePendingBindings()
            }
        }
    }


    private class ListAdapterDiffCallback : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem.id_ == oldItem.id_
        }

        override fun areContentsTheSame(
            oldItem: Measurement,
            newItem: Measurement
        ): Boolean {
            return oldItem.id_ == newItem.id_
        }


    }



}
