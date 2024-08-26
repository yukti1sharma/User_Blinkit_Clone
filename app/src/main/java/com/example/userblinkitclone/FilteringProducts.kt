package com.example.userblinkitclone

import android.widget.Filter
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.models.Product
import java.util.*
import kotlin.collections.ArrayList

class FilteringProducts(
    val adapter : AdapterProduct,
    val filter : ArrayList< Product>
) : Filter(){
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if(!constraint.isNullOrEmpty()){
            val filteredList = ArrayList<Product>()

            val query = constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")

            for(products in filter){
                if(query.any {
                        products.productTitle?.uppercase(Locale.getDefault())?.contains(it) == true ||
                                products.productCategory?.uppercase(Locale.getDefault())?.contains(it) == true ||
                                products.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(it) == true ||
                                products.productType?.uppercase(Locale.getDefault())?.contains(it) == true
                    }){
                    filteredList.add(products)
                }
            }
            result.values = filteredList
            result.count = filteredList.size

        }
        else
        {
            result.values = filter
            result.count = filter.size
        }
        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<Product>)
    }
}