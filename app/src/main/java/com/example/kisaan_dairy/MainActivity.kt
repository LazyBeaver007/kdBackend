package com.example.kisaan_dairy

import android.content.Intent
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kisaan_dairy.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    //private lateinit var binding: AppMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: View



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main)

        buttonLogin = findViewById<Button>(R.id.buttonLogin)
        progressBar = findViewById<View>(R.id.progressBar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            checkUserRoleAndNavigate(auth.currentUser!!.uid)
        }

        val progressBar = findViewById<View>(R.id.progressBar)

        buttonLogin.setOnClickListener {

            val email = findViewById<EditText>(R.id.editTextEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString().trim()

            if(email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInUser(email, password)
        }
    }

    private fun signInUser(email: String, password: String) {
        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserRoleAndNavigate(auth.currentUser!!.uid)
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }

    }


    private fun checkUserRoleAndNavigate(uid: String) {
        setLoading(true)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        val intent = Intent(this, AdminActivity::class.java)
                       startActivity(intent)

                    } else if (role == "staff") {
                        val intent = Intent(this, StaffDashboardActivity::class.java)
                        startActivity(intent)

                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        val progressBar = findViewById<View>(R.id.progressBar)
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonLogin.isEnabled = !isLoading
    }
}


