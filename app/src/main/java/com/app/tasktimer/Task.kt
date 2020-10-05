package com.app.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name : String ,val description : String , val sortOder : Int , var id : Long = 0) : Parcelable {

}