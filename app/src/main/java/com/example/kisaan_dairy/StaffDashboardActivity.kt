package com.example.kisaan_dairy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.kisaan_dairy.databinding.ActivityStaffmainBinding as ActivityStaffDashboardBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class StaffDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardRecordMilk.setOnClickListener {

             val intent = Intent(this, QuickEntryActivity::class.java)
             intent.putExtra("TRANSACTION_TYPE", "Milk")
             startActivity(intent)
        }

        binding.cardRecordFeed.setOnClickListener {
            // TODO: Navigate to the transaction flow for feed
            // val intent = Intent(this, QuickEntryActivity::class.java)
            // intent.putExtra("TRANSACTION_TYPE", "FEED")
            // startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Closes all activities
        }
    }
}