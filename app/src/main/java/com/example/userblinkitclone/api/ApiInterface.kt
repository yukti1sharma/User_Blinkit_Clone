package com.example.userblinkitclone.api

import com.example.userblinkitclone.models.CheckStatus
import com.example.userblinkitclone.models.Notification
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @GET("apis/pg-sandbox/pg/v1/status/{merchantId}/{transactionId}")

    suspend fun checkStatus(
        @HeaderMap headers : Map<String, String>,
        @Path("merchantId") merchantId : String,
        @Path("transactionId") transactionId : String
    ) : Response<CheckStatus>

    // end point of API request -- sending notification

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAARohnK0I:APA91bHmH881W9pm7rXMfGcqLqwfSSA2txbFASCNN5B8MMe1BDeInyz9vhjUm4Pf0wtZz2MMoyNgJuy84VuI-CSDw7u6VJjk8y_OU4QqZXz1ZC2X-4uz_R3E3XLJOk02xnSS06FGrjHb"
    )
    @POST("fcm/send")

    fun sendNotification(@Body notification: Notification) : Call<Notification>

}