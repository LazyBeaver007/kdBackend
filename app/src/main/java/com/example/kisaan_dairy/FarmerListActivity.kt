package com.example.kisaan_dairy

import Adapters.FarmerAdapter
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kisaan_dairy.databinding.ActivityFarmerListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import model.Farmer

class FarmerListActivity : AppCompatActivity() {


    private  lateinit var binding: ActivityFarmerListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var farmerAdapter: FarmerAdapter
    private  var allFarmers = mutableListOf<Farmer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityFarmerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setContentView(R.layout.activity_farmer_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchFarmers()
        setupSearchView()

        binding.fabAddFarmer.setOnClickListener {
            // Handle the click event for adding a new farmer
            Toast.makeText(this, "Add Farmer Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView()
    {
        farmerAdapter = FarmerAdapter(emptyList()) {farmer ->
            // Handle item click event
            Toast.makeText(this, "Item Clicked: ${farmer.name}", Toast.LENGTH_SHORT).show()
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
                    allFarmers.addAll(snapshots.toObjects<Farmer>())
                    farmerAdapter.updateData(allFarmers)
                }
            }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
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