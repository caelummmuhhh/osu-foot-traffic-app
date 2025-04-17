package com.example.osufoottrafficapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.osufoottrafficapp.R
import com.example.osufoottrafficapp.ui.adapter.MarkerAdapter
import com.example.osufoottrafficapp.ui.viewmodel.MarkerViewModel

class MarkersFragment : Fragment() {

    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MarkerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_markers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MarkerAdapter()
        recyclerView.adapter = adapter

        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)

        // Observe the markers and update the RecyclerView
        markerViewModel.allMarkers.observe(viewLifecycleOwner, Observer { markers ->
            adapter.submitList(markers)
        })

        // Find delete button and set click listener
        val deleteAllButton: Button = view.findViewById(R.id.deleteAllMarkersButton)
        deleteAllButton.setOnClickListener {
            markerViewModel.deleteAllMarkers()
        }
    }
}
