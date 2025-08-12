package com.example.kisaan_dairy

import Adapters.FarmerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kisaan_dairy.databinding.ActivityFarmerListBinding


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import model.Farmer

class FarmerListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmerListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var farmerAdapter: FarmerAdapter
    private val allFarmers = mutableListOf<Farmer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding and set the content view
        binding = ActivityFarmerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup UI components
        setupRecyclerView()
        fetchFarmers()
        setupSearchView()

        // Set click listener for the Floating Action Button
        binding.fabAddFarmer.setOnClickListener {
            startActivity(Intent(this, AddEditFarmerActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        farmerAdapter = FarmerAdapter(emptyList()) { farmer ->
            // Navigate to AddEditFarmerActivity with farmer data for editing
            val intent = Intent(this, AddEditFarmerActivity::class.java)
            intent.putExtra("FARMER_EXTRA", farmer)
            startActivity(intent)
        }
        binding.recyclerViewFarmers.apply {
            layoutManager = LinearLayoutManager(this@FarmerListActivity)
            adapter = farmerAdapter
        }
    }

    private fun fetchFarmers() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("farmers")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error fetching farmers: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    allFarmers.clear()
                    // **CRITICAL FIX:** Map Firestore documents to Farmer objects
                    // and manually assign the document ID to the model's 'id' field.
                    val farmersList = snapshots.map { doc ->
                        doc.toObject(Farmer::class.java).apply { id = doc.id }
                    }
                    allFarmers.addAll(farmersList)
                    farmerAdapter.updateData(allFarmers)
                }
            }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFarmers(newText)
                return true
            }
        })
    }

    private fun filterFarmers(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            allFarmers
        } else {
            allFarmers.filter {
                it.name.contains(query, ignoreCase = true) || it.userId.contains(query, ignoreCase = true)
            }
        }
        farmerAdapter.updateData(filteredList)
    }
}
