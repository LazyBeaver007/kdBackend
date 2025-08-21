package com.example.kisaan_dairy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class AdminActivity : AppCompatActivity() {

    private lateinit var FarmerCard: CardView
    private lateinit var StaffCard: CardView
    private lateinit var PriceFeedCard: CardView
    private lateinit var BillingCard: CardView
    private lateinit var logout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        FarmerCard = findViewById(R.id.cardManageFarmers)
        StaffCard = findViewById(R.id.cardManageStaff)
        PriceFeedCard = findViewById(R.id.cardManagePrices)
        BillingCard = findViewById(R.id.cardBilling)
        logout = findViewById(R.id.buttonLogout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        FarmerCard.setOnClickListener {
            val intent = Intent(this, FarmerListActivity::class.java)
            startActivity(intent)
        }

        StaffCard.setOnClickListener {
            val intent = Intent(this, StaffActivity::class.java)
            startActivity(intent)
        }

        PriceFeedCard.setOnClickListener {
            // Handle Price Feed Card click
        }

        BillingCard.setOnClickListener {
            // Handle Billing Card click
        }
        logout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

    }

}