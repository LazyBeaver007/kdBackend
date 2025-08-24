package com.example.kisaan_dairy

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kisaan_dairy.R
import com.example.kisaan_dairy.databinding.ActivityRecordTransactionBinding
import com.google.firebase.firestore.FirebaseFirestore
import model.Farmer
import model.FeedTransaction
import model.FeedType
import model.MilkTransaction
import java.util.Date

class RecordTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordTransactionBinding
    private lateinit var db: FirebaseFirestore
    private var selectedFarmer: Farmer? = null
    private var transactionType: String? = null
    private var feedTypesList = listOf<FeedType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Get data from previous activity
        @Suppress("DEPRECATION")
        selectedFarmer = intent.getParcelableExtra("FARMER_EXTRA")
        transactionType = intent.getStringExtra("TRANSACTION_TYPE")

        if (selectedFarmer == null || transactionType == null) {
            Toast.makeText(this, "Error: Farmer or Transaction Type missing.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        binding.buttonSaveTransaction.setOnClickListener { validateAndShowConfirmation() }
    }

    private fun setupUI() {
        // Setup common farmer info
        binding.toolbar.title = "Record $transactionType for ${selectedFarmer!!.name}"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.textViewFarmerName.text = selectedFarmer!!.name
        binding.textViewFarmerId.text = selectedFarmer!!.userId
        if (!selectedFarmer?.photoBase64.isNullOrEmpty()) {
            val decodedString = Base64.decode(selectedFarmer?.photoBase64, Base64.DEFAULT)
            Glide.with(this).asBitmap().load(decodedString).into(binding.imageViewFarmer)
        }

        // Setup UI based on transaction type
        if (transactionType == "Milk") {
            binding.radioGroupSession.visibility = View.VISIBLE
            binding.spinnerFeedType.visibility = View.GONE
            binding.quantityInputLayout.hint = "Quantity (Litres)"
        } else { // Feed
            binding.radioGroupSession.visibility = View.GONE
            binding.spinnerFeedType.visibility = View.VISIBLE
            binding.quantityInputLayout.hint = "Quantity"
            fetchFeedTypes()
        }
    }

    private fun fetchFeedTypes() {
        db.collection("feed_types").whereEqualTo("isActive", true).get()
            .addOnSuccessListener { documents ->
                feedTypesList = documents.toObjects(FeedType::class.java)
                val feedNames = feedTypesList.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, feedNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerFeedType.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load feed types.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateAndShowConfirmation() {
        val quantityStr = binding.editTextQuantity.text.toString()
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity.", Toast.LENGTH_SHORT).show()
            return
        }
        val quantity = quantityStr.toDouble()

        // Build the confirmation message
        val confirmationMessage = if (transactionType == "Milk") {
            val session = if (binding.radioButtonMorning.isChecked) "Morning" else "Evening"
            "Confirm saving $quantity Litres of Milk for ${selectedFarmer?.name} in the $session session?"
        } else {
            if (feedTypesList.isEmpty()) {
                Toast.makeText(this, "No feed types available.", Toast.LENGTH_SHORT).show()
                return
            }
            val selectedFeed = feedTypesList[binding.spinnerFeedType.selectedItemPosition]
            "Confirm saving $quantity ${selectedFeed.unit}(s) of ${selectedFeed.name} for ${selectedFarmer?.name}?"
        }

        // Show the confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirm Transaction")
            .setMessage(confirmationMessage)
            .setPositiveButton("Save") { _, _ ->
                performSaveTransaction(quantity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSaveTransaction(quantity: Double) {
        setLoading(true)

        if (transactionType == "Milk") {
            val session = if (binding.radioButtonMorning.isChecked) "Morning" else "Evening"
            val milkTransaction = MilkTransaction(
                farmerId = selectedFarmer!!.id,
                quantity = quantity,
                session = session,
                date = Date()
            )
            db.collection("milk_transactions").add(milkTransaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Milk entry saved!", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the previous screen
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else { // Feed
            val selectedFeed = feedTypesList[binding.spinnerFeedType.selectedItemPosition]
            val feedTransaction = FeedTransaction(
                farmerId = selectedFarmer!!.id,
                feedTypeId = selectedFeed.id,
                quantity = quantity,
                priceAtTransaction = selectedFeed.price,
                date = Date()
            )
            db.collection("feed_transactions").add(feedTransaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feed entry saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSaveTransaction.isEnabled = !isLoading
    }
}
