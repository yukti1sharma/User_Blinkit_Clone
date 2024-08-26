package com.example.userblinkitclone.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.activity.AuthMainActivity
import com.example.userblinkitclone.databinding.AddressBookLayoutBinding
import com.example.userblinkitclone.databinding.FragmentProfileBinding
import com.example.userblinkitclone.viewmodels.UserViewModel


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel : UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)

        onBackButtonClicked()
        onOrdersLayoutClicked()
        onAddressBookClicked()
        onLogoOutClicked()

        return binding.root
    }

    private fun onLogoOutClicked() {
        binding.LlLogOut.setOnClickListener {

            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = builder.create()

                builder.setTitle("Log out")
                .setMessage("Do you want to log out ?")
                .setPositiveButton("Yes"){_,_->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                }
                .setNegativeButton("No"){_,_->
                    alertDialog.dismiss()
                }
                    .show()
                    .setCancelable(false)
        }
    }

    private fun onAddressBookClicked() {
        binding.llAddress.setOnClickListener {
            val addressBookLayoutBinding = AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))

            viewModel.getUserAddress {address->
                addressBookLayoutBinding.etAddress.setText(address.toString())
            }

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()

            alertDialog.show()

            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled = true
            }

            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())

                alertDialog.dismiss()

                Utils.showToast(requireContext(), "Address updates...")
            }
        }
    }

    private fun onOrdersLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }

    private fun onBackButtonClicked() {
        binding.TbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)

        }
    }

}