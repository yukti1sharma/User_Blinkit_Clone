package com.example.userblinkitclone.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.CartListener
import com.example.userblinkitclone.Constants
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterBestseller
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.adapters.adapterCategory
import com.example.userblinkitclone.databinding.BsSeeAllBinding
import com.example.userblinkitclone.databinding.FragmentHomeBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Bestseller
import com.example.userblinkitclone.models.Category
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterBestseller: AdapterBestseller
    private lateinit var adapterProduct: AdapterProduct
    private var cartListener : CartListener ? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        //setStatusBarColor()

        sellAllCategories()
        navigatingToSearchFragment()
        onProfileClicked()
        fetchBestSellers()

        return binding.root
    }

    private fun fetchBestSellers() {

        binding.shimmerViewContainer.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.fetchProductType().collect{
                adapterBestseller = AdapterBestseller(::onSeeAllButtonClicked)
                binding.rvBestSellers.adapter = adapterBestseller
                adapterBestseller.differ.submitList(it)

                binding.shimmerViewContainer.visibility = View.GONE
            }

        }
    }

    private fun onProfileClicked() {
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }


    private fun navigatingToSearchFragment() {
        binding.searchCV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun sellAllCategories() {
        val categoryList = ArrayList<Category>()

        // 20 objects will be created
        for(i in 0  until  Constants.allProductsCategoryIcon.size){
            categoryList.add(Category(Constants.allProductsCategory[i], Constants.allProductsCategoryIcon[i]))
        }

        // recycler view ko list recieve ho jaayega
        binding.rvCategory.adapter = adapterCategory(categoryList, ::onCategoryIconClicked)
    }

    private fun onCategoryIconClicked(category: Category)
    {
        val bundle = Bundle()
        bundle.putString("category", category.title)

        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
    }

    fun onSeeAllButtonClicked(productType : Bestseller){
        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))
        val bs = BottomSheetDialog(requireContext())
            bs.setContentView(bsSeeAllBinding.root)

        adapterProduct = AdapterProduct(::onAddButtonCLicked, ::onIncrementButtonClicked, ::onDecrementButtonClicked)

        bsSeeAllBinding.rvProducts.adapter = adapterProduct
        adapterProduct.differ.submitList(productType.products)

        bs.show()
    }

    private fun onAddButtonCLicked(product: Product, productBinding: ItemViewProductBinding)
    {
        productBinding.tvAdd.visibility = View.GONE
        productBinding.llProductCount.visibility = View.VISIBLE

        // STEP 1
        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)


        // STEP 2  -- how to save itemcount
        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)

            viewModel.updateItemCount(product, itemCount)
        }
    }

    private fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if(product.productStock!! + 1 > itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            // STEP 2  -- how to save itemcount
            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product, itemCountInc)
            }
        }

        else{
            Utils.showToast(requireContext(), "Can't add more item of this")
        }

    }

    private fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product, itemCountDec)
        }

        if(itemCountDec > 0)
        {
            productBinding.tvProductCount.text = itemCountDec.toString()
        }
        else{
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
            productBinding.tvProductCount.text = "0"
        }
        cartListener?.showCartLayout(-1)

        // STEP 2  -- how to save itemcount

    }

    private fun saveProductInRoomDb(product: Product) {
        val cartProduct = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "₹" + "${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageURIs?.get(0)!!,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            //productType = product.productType
        )

        lifecycleScope.launch {
            viewModel.insertCartProducts(cartProduct)
        }

    }

    override fun onAttach(context: Context){
        super.onAttach(context)

        if(context is CartListener){
            cartListener = context
        }
        else
        {
            throw ClassCastException("Please implement cart listener")
        }

    }


}