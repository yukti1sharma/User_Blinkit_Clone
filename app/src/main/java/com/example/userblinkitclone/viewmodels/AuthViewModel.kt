package com.example.userblinkitclone.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.models.Users
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    // stateflow ke case hume initially ek value provide karna padta h jo ki default value ho in the ()
    // ? add karne se string non nullable ban gyi

    // to hold the verification ID obtained during an authentication process and to notify observers (such as UI components) when the verification ID changes.
    private val _verificationId = MutableStateFlow<String?>(null)
    private val _otpSent = MutableStateFlow(false)
    //exposing otpsent -- means ki hum iski value bahar files main karenge
    val otpSent: MutableStateFlow<Boolean> = _otpSent

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    private val _isACurrentUser = MutableStateFlow(false)
    val isACurrentUser = _isACurrentUser

    // agar user null ni h
    // checks if there is a current user or not
    init {
        Utils.getAuthInstance().currentUser?.let {
            _isACurrentUser.value = true
        }
    }

    fun sendOTP(userNumber: String, activity: Activity)
    {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true

            }
        }

        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+91$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {

        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            user.userToken = it.result
            Utils.getAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(user.uid?: "").setValue(user)
                        //adminToken
                        _isSignedInSuccessfully.value = true
                    } else {

                    }

                }
        }


    }

//    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {
//        val verificationId = _verificationId.value
//        if (verificationId != null) {
//            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
//            Utils.getAuthInstance().signInWithCredential(credential)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(user.uid!!).setValue(user)
//                        _isSignedInSuccessfully.value = true
//                    } else {
//                        // Handle unsuccessful sign-in
//                    }
//                }
//        } else {
//            // Handle null verificationId
//            // Log an error or handle it in a way appropriate for your app
//        }
//    }



}

