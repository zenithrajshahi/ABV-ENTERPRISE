package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BillPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface BillPaymentDao {
    @Query("SELECT * FROM bill_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<BillPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: BillPayment): Long

    @Query("DELETE FROM bill_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Int)

    @Query("DELETE FROM bill_payments")
    suspend fun clearAllPayments()
}
