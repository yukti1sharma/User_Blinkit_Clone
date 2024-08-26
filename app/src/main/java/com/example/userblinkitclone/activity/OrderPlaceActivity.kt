package com.example.userblinkitclone.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.userblinkitclone.CartListener
import com.example.userblinkitclone.Constants
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.userblinkitclone.databinding.AddressLayoutBinding
import com.example.userblinkitclone.models.Orders
import com.example.userblinkitclone.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

class OrderPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderPlaceBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var b2BPGRequest: B2BPGRequest
    private var cartListener: CartListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setStatusBarColor()
        backToUserMainActivity()
        initializePhonePe()
        getAllCartProducts()
        onPlaceOrderClicked()
    }

    private fun initializePhonePe() {
        val data = JSONObject().apply {
            put("merchantId", Constants.MERCHANTID)
            put("merchantTransactionId", Constants.merchantTransactionId)
            put("amount", 200)
            put("mobileNumber", "9310986044")
            put("callbackUrl", "https://webhook.site/callback-url")

            val paymentInstrument = JSONObject().apply {
                put("type", "UPI_INTENT")
                put("targetApp", "com.phonepe.simulator")
            }
            put("paymentInstrument", paymentInstrument)

            val deviceContext = JSONObject().apply {
                put("deviceOS", "ANDROID")
            }
            put("deviceContext", deviceContext)
        }

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1";

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this) { status ->
                if (status) {
                    // Payment work
                    getPaymentView()
                } else {
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding)
                    }
                }
            }
        }
    }

    private val phonePeView =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // UPI pin has been entered
            if (it.resultCode == 1) {
                checkPaymentStatus()
            }
        }

    private fun checkPaymentStatus() {
        val xVerify =
            sha256("/pg/v1/status/${Constants.MERCHANTID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"

        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANTID,
        )
        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect { status ->
                if (status) {
                    Utils.showToast(this@OrderPlaceActivity, "Payment done")
                    saveOrder()
                    viewModel.deleteCartProducts()
                    viewModel.savingCartItemCount(0)
                    cartListener?.hideCartLayout()

                    Utils.hideDialog()
                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                    finish()
                } else {
                    Utils.showToast(this@OrderPlaceActivity, "Payment not done")
                }
            }
        }
    }

    private fun saveOrder() {
        viewModel.getAll().observe(this) { cartProductsList ->
            if (!cartProductsList.isEmpty()) {
                viewModel.getUserAddress { address ->
                    val order = Orders(
                        orderId = Utils.getRandomId(),
                        orderList = cartProductsList,
                        userAddress = address,
                        orderDate = Utils.getCurrentDate(),
                        orderStatus = 0,
                        orderingUserId = Utils.getCurrentUserid()
                    )

                    viewModel.saveOrderedProducts(order)

                    // Notification
                    lifecycleScope.launch {
                        viewModel.sendNotification(
                            cartProductsList[0].adminUid!!,
                            "Ordered",
                            "Some products has been ordered"
                        )
                    }
                }

                for (products in cartProductsList) {
                    val count = products.productCount
                    val stock = products.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductsAfterOrder(stock, products)
                    }
                }
            }
        }
    }

    private fun getPaymentView() {
        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")
                .let {
                    phonePeView.launch(it)
                }
        } catch (e: PhonePeInitException) {
            Utils.showToast(this, e.message.toString())
        }
    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing....")

        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAdress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"

        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }

        Utils.showToast(this, "Saved..")
        alertDialog.dismiss()

        getPaymentView()
    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this, UsersMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) { cartProductsList ->

            adapterCartProducts = AdapterCartProducts()
            binding.rvProductItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductsList)

            var totalPrice = 0

            for (products in cartProductsList) {
                val price = products.productPrice?.substring(1)?.toIntOrNull()
                val itemCount = products.productCount ?: 0
                if (price != null) {
                    totalPrice += price * itemCount
                }
            }

            binding.tvSubTotal.text = totalPrice.toString()

            if (totalPrice < 200) {
                binding.tvDeliveryCharges.text = "₹15"
                totalPrice += 15
            }

            binding.tvGrandTotal.text = totalPrice.toString()
        }
    }

    private fun setStatusBarColor() {
        window?.apply {
            var statusBarColor = ContextCompat.getColor(this@OrderPlaceActivity, R.color.yellow)
            statusBarColor = statusBarColor

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
