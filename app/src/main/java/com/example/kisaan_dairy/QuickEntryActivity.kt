package com.example.kisaan_dairy

import Adapters.QuickEntryAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kisaan_dairy.databinding.ActivityQuickEntryBinding

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import model.Farmer

class QuickEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickEntryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var quickEntryAdapter: QuickEntryAdapter
    private val allFarmers = mutableListOf<Farmer>()
    private var transactionType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionType = intent.getStringExtra("TRANSACTION_TYPE")
        if (transactionType == null) {
            Toast.makeText(this, "Error: Transaction type not specified.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = FirebaseFirestore.getInstance()
        setupUI()
        setupRecyclerView()
        fetchActiveFarmers()
        setupSearchView()
    }

    private fun setupUI() {
        binding.toolbar.title = "Select Farmer for $transactionType Entry"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonScanQr.setOnClickListener {
            // TODO: Implement QR Code Scanning
            Toast.makeText(this, "QR Scan coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        quickEntryAdapter = QuickEntryAdapter(emptyList()) { farmer ->
            // Farmer selected, proceed to the next step
            navigateToTransactionScreen(farmer)
        }
        binding.recyclerViewFarmers.apply {
            layoutManager = LinearLayoutManager(this@QuickEntryActivity)
            adapter = quickEntryAdapter
        }
    }

    private fun fetchActiveFarmers() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("farmers")
            .whereEqualTo("active", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    Log.e("FirestoreData", "Listen failed.", error)
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("FirestoreData", "Received ${snapshots.size()} documents.")
                    allFarmers.clear()
                    val farmersList = snapshots.map { doc -> doc.toObject(Farmer::class.java).apply { id = doc.id } }
                    allFarmers.addAll(farmersList)
                    quickEntryAdapter.updateData(allFarmers)
                    Log.d("FirestoreData", "Adapter updated with ${farmersList.size} farmers.")
                } else {
                    Log.d("FirestoreData", "Snapshots data is null.")
                }
            }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
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
        quickEntryAdapter.updateData(filteredList)
    }

    private fun navigateToTransactionScreen(farmer: Farmer) {
        val intent = Intent(this, RecordTransactionActivity::class.java).apply {
            putExtra("FARMER_EXTRA", farmer)
            putExtra("TRANSACTION_TYPE", transactionType)
        }
        startActivity(intent)
    }
}