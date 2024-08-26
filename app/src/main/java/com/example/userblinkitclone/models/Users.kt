package com.example.userblinkitclone.models

data class Users(
    val uid : String ? = null,
    val userPhoneNumber : String ? = null,
    val userAddress : String ? = null,
    var userToken : String ?= null
)
