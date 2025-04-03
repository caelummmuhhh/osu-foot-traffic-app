package com.example.osufoottrafficapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.osufoottrafficapp.R
import com.example.osufoottrafficapp.ui.fragment.MarkerEntity

class MarkerAdapter : ListAdapter<MarkerEntity, MarkerAdapter.MarkerViewHolder>(MarkerDiffCallback()) {

    class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.marker_name)
        val coordsTextView: TextView = itemView.findViewById(R.id.marker_coords)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marker, parent, false)
        return MarkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val marker = getItem(position)
        holder.nameTextView.text = marker.title
        holder.coordsTextView.text = "Lat: ${marker.latitude}, Lng: ${marker.longitude}"
    }
}

class MarkerDiffCallback : DiffUtil.ItemCallback<MarkerEntity>() {
    override fun areItemsTheSame(oldItem: MarkerEntity, newItem: MarkerEntity): Boolean {
        return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
    }

    override fun areContentsTheSame(oldItem: MarkerEntity, newItem: MarkerEntity): Boolean {
        return oldItem == newItem
    }
}
