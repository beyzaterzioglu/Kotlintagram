package com.beyzaterzioglu.kotlintagram.modal

import com.google.firebase.Timestamp

data class SubComment(
    val userid :String?="",
    val userName: String = "",
    val commentText: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
{

}