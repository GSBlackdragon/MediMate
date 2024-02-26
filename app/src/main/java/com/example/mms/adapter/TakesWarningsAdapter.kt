package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.database.inApp.AppDatabase

/**
 * Adapter for the recycler view of the takes warnings
 *
 * @param context the context of the activity
 * @param warnings the list of warnings
 * @param allergyName the name of the potential allergy
 */
class TakesWarningsAdapter(
    private val db : AppDatabase,
    private val context: Context,
    private val warnings: List<String>,
    private val allergyName : String
    ) :

    RecyclerView.Adapter<TakesWarningsAdapter.MyViewHolder>() {
        // interface for the click listener
    private var itemClickListener: OnItemClickListener? = null

    // function that sets the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    // class that represents the view holder of the recycler view
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconWarning: ImageView = itemView.findViewById(R.id.icon_warning)
    }

    // function that creates the view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_warnings, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = warnings[position]

        //Displaying the right icon regarding the warning type
        when(item){
            "allaitement" -> holder.iconWarning.setImageResource(R.drawable.icon_allaitement)
            "enceinte" -> holder.iconWarning.setImageResource(R.drawable.icon_femme_enceinte)
            "enceintes" -> holder.iconWarning.setImageResource(R.drawable.icon_femme_enceinte)
            "enfant" -> holder.iconWarning.setImageResource(R.drawable.icon_risque_enfant)
            "allergie" -> holder.iconWarning.setImageResource(R.drawable.icon_allergie)
        }
        if (item.matches(Regex("^6.*",RegexOption.IGNORE_CASE))){
            holder.iconWarning.setImageResource(R.drawable.contreindic)
        }

        //Attaching the right toast to the item regarding the warning type
        holder.itemView.setOnClickListener {
            when(item){
                "allaitement" -> Toast.makeText(context, R.string.toast_allaitement, Toast.LENGTH_LONG).show()
                "enceinte" -> Toast.makeText(context, R.string.toast_femme_enceinte, Toast.LENGTH_LONG).show()
                "enceintes" -> Toast.makeText(context, R.string.toast_femme_enceinte, Toast.LENGTH_LONG).show()
                "enfant" -> Toast.makeText(context, R.string.toast_risque_enfant, Toast.LENGTH_LONG).show()
                "allergie" -> Toast.makeText(context, context.getString(R.string.toast_allergie,allergyName), Toast.LENGTH_LONG).show()
            }
            if (item.matches(Regex("^6.*",RegexOption.IGNORE_CASE))) {
                var nom : String? = ""
                var t = Thread {
                    nom = db.medicineDao().getByCIS(item.toLong())?.name
                }
                t.start()
                t.join()
                Toast.makeText(context, context.getString(R.string.toast_contre_indic,nom), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return warnings.size
    }
}
