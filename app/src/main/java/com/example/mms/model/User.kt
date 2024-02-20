package com.example.mms.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity

@Entity(tableName = "user", primaryKeys = ["email"])
data class User (
    var name: String,
    var surname: String,
    val email: String,
    var birthday: String,
    val sexe: String,
    var weight: Int,
    var height: Int,
    val isConnected : Boolean = false,
    var codePin : String,
    var listHealthDiseases : String,
    var listDietPlan : String,
    var listAllergies : String,
    var isLinkedToBiometric : Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun toString(): String {
        return "$name $surname"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(name)
        p0.writeString(surname)
        p0.writeString(email)
        p0.writeString(birthday)
        p0.writeString(sexe)
        p0.writeInt(weight)
        p0.writeInt(height)
        p0.writeByte(if (isConnected) 1 else 0)
        p0.writeString(codePin)
        p0.writeString(listHealthDiseases)
        p0.writeString(listDietPlan)
        p0.writeString(listAllergies)
        p0.writeByte(if (isLinkedToBiometric) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}