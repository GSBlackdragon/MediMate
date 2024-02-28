package com.example.mms.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.adapter.AccountAdapter
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.ActivityLoginBinding
import com.example.mms.ui.createAccount.CreateAccountActivity
import com.example.mms.ui.locked.LockedActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SingletonDatabase.getDatabase(this)

        Thread {
            // get all users
            val users = db.userDao().getAllUsers()
            adapter = AccountAdapter(this, users)

            runOnUiThread {
                // set adapter
                binding.accountRecyclerView.adapter = adapter
                binding.accountRecyclerView.layoutManager = LinearLayoutManager(this)

                binding.accountRecyclerView.layoutManager = LinearLayoutManager(this)
            }

            // check if there is a connected user
            val connectedUser = users.find { it.isConnected }
            if (connectedUser != null) {
                // if there is a connected user, go to locked activity
                val intent = Intent(this, LockedActivity::class.java)
                intent.putExtra("userEmail", connectedUser.email).putExtra("isLinkedToBiometric", connectedUser.isLinkedToBiometric)
                startActivity(intent)
                finish()
            }

            // set on click listener
            adapter.setOnItemClickListener { position ->
                val intent = Intent(this@LoginActivity, LockedActivity::class.java)
                intent.putExtra("userEmail", users[position].email)
                    .putExtra("isLinkedToBiometric", users[position].isLinkedToBiometric)
                startActivity(intent)
                finish()
            }
        }.start()

        binding.buttonCreateAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
