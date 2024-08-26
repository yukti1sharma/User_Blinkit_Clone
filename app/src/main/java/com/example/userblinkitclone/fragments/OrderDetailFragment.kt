package com.example.userblinkitclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.databinding.FragmentOrderDetailBinding
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailBinding
    private var status = 0
    private var orderId = ""
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)

        getValues()
        settingStatus()
        getOrderedProducts()   // shayad yahan lifecyclescope.apply lagega
        onBackButtonClicked()

        return binding.root
    }

    private fun getOrderedProducts() {
        lifecycleScope.launch {
            viewModel.getOrderedProducts(orderId).collect{cartList->
                adapterCartProducts = AdapterCartProducts()
                binding.rvProductItems.adapter = adapterCartProducts
                adapterCartProducts.differ.submitList(cartList)
            }
        }

    }

    private fun settingStatus() {
        val viewsMap = mapOf(
            0 to listOf(binding.iv1),
            1 to listOf(binding.iv1, binding.iv2, binding.view1),
            2 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2),
            3 to listOf(binding.iv1, binding.iv2, binding.view1, binding.iv3, binding.view2, binding.iv4, binding.view3)
        )

        val viewsToInt = viewsMap.getOrDefault(status, emptyList())

        for(view in viewsToInt){
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
        }
    }


    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status")!!
        orderId = bundle.getString("orderId").toString()
    }

    private fun onBackButtonClicked() {
        binding.TbOrdersDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_ordersFragment)

        }
    }

}