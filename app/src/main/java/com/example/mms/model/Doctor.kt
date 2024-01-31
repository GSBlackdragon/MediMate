package com.example.mms.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(
    tableName = "Doctor",

)
@Serializable
class Doctor (
    @PrimaryKey var pp: Long,
    var npp: Long,
    var name: String,
    var firstname: String,
    var typeDiplome: String,
    var codeDiplome:String,
    var libelleDiplome:String){

    override fun toString(): String {
        return "Doctor(PP=$pp, NPP=$npp, Nom=$name, Prenom=$firstname, TypeDiplome=$typeDiplome, CodeDiplome=$codeDiplome, LibelleDiplome='$libelleDiplome')"
    }
}