package com.example.userblinkitclone.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.activity.UsersMainActivity
import com.example.userblinkitclone.databinding.FragmentOTPBinding
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {

    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOTPBinding.inflate(layoutInflater)

        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing you...")

            val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
            val otp = editTexts.joinToString("") {it.text.toString() }

            if(otp.length < editTexts.size){
                Utils.showToast(requireContext(), "Please enter right OTP")
            }
            else{
                editTexts.forEach { it.text?.clear();it.clearFocus() }
                verifyOTP(otp)
            }
        }
    }

    private fun verifyOTP(otp: String) {

        val user = Users(uid = Utils.getCurrentUserid(), userPhoneNumber = userNumber, userAddress = " ")

        viewModel.signInWithPhoneAuthCredential(otp, userNumber, user)
        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect{
                if(it)
                {
                    Utils.hideDialog()
                    Utils.showToast(requireContext(), "Logged in...")

                    startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }


    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP...")

        viewModel.apply {
            sendOTP(userNumber, requireActivity())
            // couroutines launch
            lifecycleScope.launch {
                otpSent.collect{
                    if(it)
                    {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "OTP sent...")
                    }
                }
            }

        }

    }

    // if back arrow if click hota h
    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for(i in editTexts.indices){
            editTexts[i].addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                // logic of agar ek edit text main likh dia toh next edit text pe automatically chal jaaye
                override fun afterTextChanged(s: Editable?) {
                    if(s?.length == 1)   // agar hum otp ko add karte hain
                    {
                        if(i < editTexts.size - 1){
                            editTexts[i+1].requestFocus()
                        }
                    }
                    else if(s?.length == 0)   // agar hum otp ko remove karte hain
                    {
                        if(i > 0)
                        {
                            editTexts[i-1].requestFocus()
                        }
                    }
                }

            })
        }
    }

    // number is getting recieved from sign in fragment in the +91 section
    private fun getUserNumber() {
        val bundle = arguments

        //? is nullability check
        userNumber = bundle?.getString("number" ).toString()

        binding.tvUserNumber.text = userNumber

    }


}