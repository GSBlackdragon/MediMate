package com.example.mms.contrat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.mms.model.User
import com.example.mms.ui.modifyAccount.ModifyAccountActivity

class ModifiyAccountContrat : ActivityResultContract<Unit?, User>() {
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, ModifyAccountActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): User {
        var res = User("","","","","",0,0,false,"","","","",false)
        if (resultCode == Activity.RESULT_OK) {
            res = intent!!.getParcelableExtra("user")!!
        }
        return res
    }
}