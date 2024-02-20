package com.example.mms.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(
    tableName = "Doctor",

    )
@Serializable
class Doctor (
    @PrimaryKey var rpps: Long,
    var name: String,
    var firstname: String,
    var title:String,
    var speciality: String,
    var email:String="",
    var city:String="",
    var zipcode:String="",
    var address:String="",
    var phoneNumber:String="",
    ){

    override fun toString(): String {
        return "Doctor(rpps=$rpps, name='$name', firstname='$firstname', title='$title', speciality='$speciality', email='$email', city='$city', zipCode='$zipcode', address='$address', phoneNumber='$phoneNumber')"
    }
}