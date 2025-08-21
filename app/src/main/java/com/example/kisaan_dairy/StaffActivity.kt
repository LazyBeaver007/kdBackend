package com.example.kisaan_dairy

import Adapters.StaffAdapter
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kisaan_dairy.databinding.ActivityStaffBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.User

class StaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var staffAdapter: StaffAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fetchStaffMembers()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.fabAddStaff.setOnClickListener { showAddStaffDialog() }
    }

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter(emptyList())
        binding.recyclerViewStaff.apply {
            layoutManager = LinearLayoutManager(this@StaffActivity)
            adapter = staffAdapter
        }
    }

    private fun fetchStaffMembers() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("users")
            .whereEqualTo("role", "staff") // Query for users with the 'staff' role
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val staffList = snapshots.toObjects(User::class.java)
                    staffAdapter.updateData(staffList)
                    binding.textViewEmpty.visibility = if (staffList.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun showAddStaffDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Staff Member")

        // Set up the input fields
        val emailInput = EditText(this)
        emailInput.hint = "Email"
        emailInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        val passwordInput = EditText(this)
        passwordInput.hint = "Password"
        passwordInput.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)
        layout.addView(emailInput)
        layout.addView(passwordInput)
        builder.setView(layout)

        // Set up the buttons
        builder.setPositiveButton("Create") { dialog, _ ->
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else {
                    createStaffUser(email, password)
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun createStaffUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        // 1. Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // 2. Create user document in Firestore with 'staff' role
                        val user = User(uid = firebaseUser.uid, email = email, role = "staff")
                        db.collection("users").document(firebaseUser.uid).set(user)
                            .addOnSuccessListener {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Staff user created successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}