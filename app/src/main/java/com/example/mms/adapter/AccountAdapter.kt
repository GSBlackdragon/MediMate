package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.Utils.cryptEmail
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.model.User

/**
 * Adapter for the recycler view of the accounts
 *
 * @param context the context of the activity
 * @param users the list of users
 */
class AccountAdapter(private val context: Context, private val users: List<User>) :
    RecyclerView.Adapter<AccountAdapter.MyViewHolder>() {
        // interface for the click listener
    private var itemClickListener: OnItemClickListener? = null

    // function that sets the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    // class that represents the view holder of the recycler view
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById<TextView>(R.id.account_name)
        val userEmail: TextView = itemView.findViewById<TextView>(R.id.account_email)
        val userImageFingerprint : ImageView = itemView.findViewById<ImageView>(R.id.account_fingerprint)
    }

    // function that creates the view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false)
        return MyViewHolder(view)
    }

    // function that returns the number of items
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = users[position]
        "${user.name} ${user.surname}".also { holder.userName.text = it }
        // we crypt the email
        holder.userEmail.text = cryptEmail(user.email)
        // we set the fingerprint image if the user is linked to biometric
        holder.userImageFingerprint.visibility = if (user.isLinkedToBiometric) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
