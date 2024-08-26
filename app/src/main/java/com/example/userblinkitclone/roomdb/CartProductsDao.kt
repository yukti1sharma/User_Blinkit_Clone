package com.example.userblinkitclone.roomdb

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProducts(products: CartProducts)

    @Update
    fun updateCartProducts(products: CartProducts)

    @Query("SELECT * FROM CartProducts")
    fun getAllCartProducts() : LiveData<List<CartProducts>>

    @Query("DELETE FROM CartProducts WHERE productId = :productId ")
    fun deleteCartProduct(productId : String)

    @Query("DELETE FROM CartProducts")
    suspend fun deleteCartProducts()

}