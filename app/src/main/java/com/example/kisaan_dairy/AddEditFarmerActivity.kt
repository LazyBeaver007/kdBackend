package com.example.kisaan_dairy

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.kisaan_dairy.databinding.ActivityAddEditFarmerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import model.Farmer
import java.util.UUID

@Suppress("DEPRECATION")
class AddEditFarmerActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAddEditFarmerBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var existingFarmer: Farmer? = null
    private var imageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imageViewFarmer.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEditFarmerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_add_edit_farmer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        existingFarmer = intent.getParcelableExtra("FARMER_EXTRA")
        setupUI()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imageViewFarmer.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.textViewChangePhoto.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.buttonSave.setOnClickListener { saveFarmer() }
    }

    private fun setupUI() {
        if (existingFarmer != null) {
            binding.toolbar.title = "Edit Farmer"
            binding.editTextName.setText(existingFarmer?.name)
            binding.editTextPhone.setText(existingFarmer?.phone)
            binding.editTextAddress.setText(existingFarmer?.address)
            binding.switchIsActive.isChecked = existingFarmer?.isActive ?: true

            if (!existingFarmer?.photoUrl.isNullOrEmpty()) {
                Glide.with(this).load(existingFarmer?.photoUrl).into(binding.imageViewFarmer)
            }
        } else {
            binding.toolbar.title = "Add New Farmer"
            binding.switchIsActive.isChecked = true
        }
    }

    private fun saveFarmer() {
        val name = binding.editTextName.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()
        val isActive = binding.switchIsActive.isChecked

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        if (imageUri != null) {
            // If a new image is selected, upload it first
            uploadImageAndSaveFarmer(name, phone, address, isActive)
        } else {
            // If no new image, just save the farmer data
            saveFarmerToFirestore(name, phone, address, isActive, existingFarmer?.photoUrl)
        }
    }

    private fun uploadImageAndSaveFarmer(name: String, phone: String, address: String, isActive: Boolean) {
        val fileName = "farmer_photos/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)

        ref.putFile(imageUri!!)
            .onSuccessTask { ref.downloadUrl }
            .addOnSuccessListener { downloadUri ->
                saveFarmerToFirestore(name, phone, address, isActive, downloadUri.toString())
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFarmerToFirestore(name: String, phone: String, address: String, isActive: Boolean, photoUrl: String?) {
        if (existingFarmer != null) {
            // Update existing farmer
            val updatedData = mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address,
                "active" to isActive,
                "photoUrl" to photoUrl
            )
            db.collection("farmers").document(existingFarmer!!.id)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Farmer updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error updating farmer: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Create new farmer
            val farmerCollection = db.collection("farmers")
            // Generate a new sequential ID
            farmerCollection.orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    val lastId = if (!querySnapshot.isEmpty) {
                        val lastFarmerId = querySnapshot.documents[0].getString("userId") ?: "KDF-1000"
                        lastFarmerId.split('-')[1].toInt()
                    } else {
                        1000 // Start from 1001 if collection is empty
                    }
                    val newUserId = "KDF-${lastId + 1}"

                    val newFarmer = Farmer(
                        id = farmerCollection.document().id, // Let Firestore generate the document ID
                        userId = newUserId,
                        name = name,
                        phone = phone,
                        address = address,
                        isActive = isActive,
                        photoUrl = photoUrl
                    )

                    farmerCollection.document(newFarmer.id).set(newFarmer)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Farmer added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            Toast.makeText(this, "Error adding farmer: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error generating farmer ID: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSave.isEnabled = !isLoading
    }
}