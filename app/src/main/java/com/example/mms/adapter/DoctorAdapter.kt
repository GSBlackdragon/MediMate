package com.example.mms.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.constant.header
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.Doctor
import com.example.mms.model.Takes
import com.example.mms.model.Task
import com.example.mms.service.TasksService
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate

class DoctorAdapter(
    private var doctorList : MutableList<Doctor>,
    val context : Context,
    val db : AppDatabase
    )  : RecyclerView.Adapter<DoctorAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomMedicine: TextView = itemView.findViewById(R.id.nomMedecin)
        val titleMedicine: TextView = itemView.findViewById(R.id.titleMedecin)
        val cityMedicine: TextView = itemView.findViewById(R.id.cityMedecin)
        val btnDelete: ImageView = itemView.findViewById(R.id.medecinDelete)
        val btnSms: TextView = itemView.findViewById(R.id.btn_sms)
        val btnMail: TextView = itemView.findViewById(R.id.btn_mail)
        val btnDlPDF : Button = itemView.findViewById(R.id.btn_dl_pdf)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_conseil_medecin, parent, false)
        return MyViewHolder(view)
    }

    fun updateList(list : List<Doctor>){
        this.doctorList=list.toMutableList()
    }
    override fun getItemCount(): Int {
        return doctorList.size
    }
    private fun pdfGenerateur(context: Context) {
        val tempo =  generateDataForReport()

        val fileName = "MedicineReport"
        var content = header.replace("TAB", tempo.first.replace("[", "").replace("]", "").replace(",", ""))
        content = content.replace("LISTTRAITEMENT", "Résumé des prises de médicaments pour le/les médicaments suivants : "+tempo.second.toString().replace("[", "").replace("]", ""))
        content = content.replace("PNAME", " "+db.userDao().getConnectedUser()!!.name.uppercase())
        content = content.replace("DATEGEN", LocalDate.now().toString())
        val drawable = context.getDrawable(R.drawable.icon)
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> null
        }


        if (bitmap != null) {

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()


            val encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            content = content.replace("LOGOPATH", "data:image/png;base64,$encodedImage")
        }
        try {

            if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
                return
            }


            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path


            val pdfFile = File(downloadsPath, "$fileName.pdf")


            HtmlConverter.convertToPdf(content, PdfWriter(pdfFile))


            Looper.prepare()
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()


        } catch (e: Exception) {
            e.printStackTrace()
            Looper.prepare()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_LONG).show()

            }
        }




    private fun generateDataForReport():Pair<String,MutableList<String>> {
        class pdf (var taskid : Int, var takes: MutableList<Takes>)


        val  weightbyname = mutableMapOf<Long,String >()
        val tasks: MutableList<Task> = TasksService(context).getCurrentUserTasks().toMutableList()

        tasks.forEach {
            TasksService(context).getTaskFilled(it, it.cycle.isEmpty() && it.specificDays.isEmpty())
        }
        Log.d("pdf", tasks.toString())
        val takes = mutableListOf<pdf>()

        val t = Thread{
        tasks.forEach { task ->

            if (task.cycle.isEmpty() && task.specificDays.isEmpty()) {

                takes.add(pdf(task.id.toInt(),db.takesDao().getAllFromHourWeightId(task.oneTakeHourWeight!!.id)))

                weightbyname.put(task.oneTakeHourWeight!!.id.toLong(),db.medicineDao().getNameByCIS(task.medicineCIS)!!)
            } else {
                if (task.cycle.isNotEmpty()) {
                    task.cycle.hourWeights.forEach { hourWeight ->

                        takes.add(pdf(task.id.toInt(),db.takesDao().getAllFromHourWeightId(hourWeight.id)))
                    }
                    weightbyname.put(task.cycle.id.toLong(),db.medicineDao().getNameByCIS(task.medicineCIS)!!)
                } else {
                    task.specificDays.forEach { specificDay ->

                        takes.add(pdf(task.id.toInt(),db.takesDao().getAllFromHourWeightId(specificDay.hourWeightId)))
                        weightbyname.put(specificDay.hourWeightId.toLong(),db.medicineDao().getNameByCIS(task.medicineCIS)!!)
                    }


                }
            }
        }


        }
        t.start()
        t.join()
        var storage = ""
        val tab = takes.groupBy { it.taskid }
        tab.forEach(){

            storage +="<p>Sur la période du ${it.value[0].takes[0].date.toLocalDate()} au ${LocalDate.now()}, le patient a pris le mdédicament <strong>${weightbyname[it.key.toLong()]}</strong> correctement <strong>${it.value.filter { it.takes[0].isDone }.size * 100 / it.value.size}</strong> % du temps</p>"
        }
        return Pair(storage, weightbyname.values.toMutableList())

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = doctorList[position]
        holder.nomMedicine.text = item.firstname.plus(" ").plus(item.name)
        holder.titleMedicine.text = item.title.ifEmpty { item.speciality }
        holder.cityMedicine.text = item.city
        holder.btnDelete.setOnClickListener {
            val tt = Thread {
                db.doctorDao().deleteDoctor(item)
            }
            tt.start()
            tt.join()
            doctorList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, doctorList.size)
            Toast.makeText(this.context, R.string.delete_medecin, Toast.LENGTH_SHORT).show()
        }
        if(item.email != "") {







            holder.btnMail.setOnClickListener {

                val mailAddress = item.email
                if (mailAddress.isNotEmpty()) {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))

                    }
                    try {
                        this.context.startActivity(emailIntent)
                    } catch (ex: Exception) {
                        Toast.makeText(this.context, this.context.getString(R.string.erreur_lors_de_ouverture_mails), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this.context, this.context.getString(R.string.aucune_adresse_mail), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.btnMail.visibility = View.GONE
        }

        if (item.phoneNumber != "") {
            holder.btnSms.setOnClickListener {
                val phoneNumber = item.phoneNumber
                if (phoneNumber.isNotEmpty()) {
                    val smsUri = Uri.parse("smsto:$phoneNumber")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
                    try {
                        this.context.startActivity(smsIntent)
                    } catch (ex: Exception) {
                        Toast.makeText(this.context, this.context.getString(R.string.erreur_ouverture_sms), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this.context, this.context.getString(R.string.aucun_numero_telephone), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.btnSms.visibility = View.GONE
        }
        holder.btnDlPDF.setOnClickListener {
            Thread{pdfGenerateur(context)}.start()

        }
    }

}