package com.example.userblinkitclone.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.activity.UsersMainActivity
import com.example.userblinkitclone.databinding.FragmentSplashBinding
import com.example.userblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding : FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)


        Handler(Looper.getMainLooper()).postDelayed(
            {

                lifecycleScope.launch {
                    viewModel.isACurrentUser.collect{
                        if(it){
                            // if currrent user exists show home fragment .. not login again and again
                            startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                            requireActivity().finish()
                        }
                        else{
                            findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                        }
                    }
                }
            }, 3000)
        return binding.root
    }

}