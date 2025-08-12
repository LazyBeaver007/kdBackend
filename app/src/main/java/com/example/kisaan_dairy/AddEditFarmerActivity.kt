package com.example.kisaan_dairy

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kisaan_dairy.databinding.ActivityAddEditFarmerBinding
import com.google.firebase.firestore.FirebaseFirestore
import model.Farmer
import java.io.ByteArrayOutputStream
import java.util.Date

class AddEditFarmerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditFarmerBinding
    private lateinit var db: FirebaseFirestore

    private var existingFarmer: Farmer? = null
    private var newImageBase64: String? = null

    // ActivityResultLauncher for picking an image from the gallery
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Convert selected image to a resized Base64 string
            newImageBase64 = uriToResizedBase64(it)
            // Decode the Base64 string back to display it
            val decodedString = Base64.decode(newImageBase64, Base64.DEFAULT)
            Glide.with(this).asBitmap().load(decodedString).into(binding.imageViewFarmer)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditFarmerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Check if we are editing an existing farmer
        @Suppress("DEPRECATION")
        existingFarmer = intent.getParcelableExtra("FARMER_EXTRA")
        setupUI()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imageViewFarmer.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.textViewChangePhoto.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.buttonSave.setOnClickListener { validateAndSaveFarmer() }
    }

    private fun setupUI() {
        if (existingFarmer != null) {
            binding.toolbar.title = "Edit Farmer"
            binding.editTextName.setText(existingFarmer?.name)
            binding.editTextPhone.setText(existingFarmer?.phone)
            binding.editTextAddress.setText(existingFarmer?.address)
            binding.switchIsActive.isChecked = existingFarmer?.isActive ?: true

            // Load photo from Base64 string
            if (!existingFarmer?.photoBase64.isNullOrEmpty()) {
                try {
                    val decodedString = Base64.decode(existingFarmer?.photoBase64, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(decodedString).into(binding.imageViewFarmer)
                } catch (e: IllegalArgumentException) {
                    // Handle case where Base64 string is invalid
                    Toast.makeText(this, "Could not load farmer photo.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.toolbar.title = "Add New Farmer"
            binding.switchIsActive.isChecked = true
        }
    }

    private fun validateAndSaveFarmer() {
        val name = binding.editTextName.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()
        val isActive = binding.switchIsActive.isChecked

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // If a new image was selected, newImageBase64 will have a value.
        // Otherwise, we use the existing photo data.
        val finalPhotoData = newImageBase64 ?: existingFarmer?.photoBase64

        saveFarmerToFirestore(name, phone, address, isActive, finalPhotoData)
    }

    private fun saveFarmerToFirestore(name: String, phone: String, address: String, isActive: Boolean, photoData: String?) {
        if (existingFarmer != null) {
            // --- UPDATE EXISTING FARMER ---
            val updatedData = mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address,
                "active" to isActive,
                "photoBase64" to photoData
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
            // --- CREATE NEW FARMER ---
            val farmerCollection = db.collection("farmers")
            // Generate a new sequential ID
            farmerCollection.orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    val lastId = if (!querySnapshot.isEmpty) {
                        val lastFarmerId = querySnapshot.documents[0].getString("userId") ?: "KDF-1000"
                        lastFarmerId.split('-')[1].toInt()
                    } else { 1000 } // Start from 1001 if collection is empty

                    val newUserId = "KDF-${lastId + 1}"
                    val docId = farmerCollection.document().id

                    val newFarmer = Farmer(
                        id = docId,
                        userId = newUserId,
                        name = name,
                        phone = phone,
                        address = address,
                        isActive = isActive,
                        photoBase64 = photoData,
                        createdAt = Date() // Set creation timestamp
                    )

                    farmerCollection.document(docId).set(newFarmer)
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

    /**
     * Converts an image Uri to a resized Bitmap and then to a Base64 string.
     */
    private fun uriToResizedBase64(uri: Uri): String? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }

            // Resize the bitmap to a max width/height to save space
            val resizedBitmap = resizeBitmap(bitmap, 600)

            val outputStream = ByteArrayOutputStream()
            // Compress the bitmap to JPEG format with 80% quality
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            // Encode the byte array to a Base64 string
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        return try {
            if (source.height >= source.width) {
                if (source.height <= maxLength) return source
                val newHeight = maxLength
                val newWidth = (source.width * (newHeight.toFloat() / source.height)).toInt()
                Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
            } else {
                if (source.width <= maxLength) return source
                val newWidth = maxLength
                val newHeight = (source.height * (newWidth.toFloat() / source.width)).toInt()
                Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
            }
        } catch (e: Exception) {
            source
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSave.isEnabled = !isLoading
    }
}
