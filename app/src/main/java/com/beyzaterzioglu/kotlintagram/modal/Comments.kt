package com.beyzaterzioglu.kotlintagram.modal

import com.google.firebase.Timestamp

data class Comments(
    val userid :String?="",
    val userName: String = "",
    val commentId : String=" ",
    val commentText: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val subComments: MutableList<SubComment> = mutableListOf()
)
{

}