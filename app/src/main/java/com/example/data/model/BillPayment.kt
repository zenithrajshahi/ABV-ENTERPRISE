package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_payments")
data class BillPayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val consumerName: String,
    val accountNo: String,
    val operator: String, // e.g. "Palli Bidyut", "DPDC", "DESCO", "NESCO", "WZPDCO"
    val billingMonth: String,
    val amount: Double,
    val contactNo: String,
    val trxId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "Pending", "Completed", "Failed"
    val paymentType: String // "Manual", "Sample"
)
