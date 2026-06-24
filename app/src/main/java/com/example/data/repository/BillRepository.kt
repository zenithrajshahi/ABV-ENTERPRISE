package com.example.data.repository

import com.example.data.local.BillPaymentDao
import com.example.data.model.BillPayment
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billPaymentDao: BillPaymentDao) {

    val allPayments: Flow<List<BillPayment>> = billPaymentDao.getAllPayments()

    suspend fun insertPayment(payment: BillPayment): Long {
        return billPaymentDao.insertPayment(payment)
    }

    suspend fun deletePaymentById(id: Int) {
        billPaymentDao.deletePaymentById(id)
    }

    suspend fun clearAllPayments() {
        billPaymentDao.clearAllPayments()
    }

    // Static sample/mock pending bills for simulation purposes
    fun getSamplePendingBills(): List<SampleBill> {
        return listOf(
            SampleBill(
                consumerName = "Abul Kalam Azad",
                accountNo = "10234567890",
                operator = "Palli Bidyut (BREB)",
                billingMonth = "June 2026",
                amount = 450.00,
                contactNo = "01712345678",
                billNo = "PB-90821-6"
            ),
            SampleBill(
                consumerName = "Rumana Akhtar",
                accountNo = "209485731",
                operator = "DPDC Postpaid",
                billingMonth = "May 2026",
                amount = 1850.50,
                contactNo = "01811223344",
                billNo = "DPDC-77412-M"
            ),
            SampleBill(
                consumerName = "Mohammad Yusuf",
                accountNo = "304567210",
                operator = "DESCO Prepaid",
                billingMonth = "June 2026",
                amount = 1000.00,
                contactNo = "01555667788",
                billNo = "DESC-55219-P"
            ),
            SampleBill(
                consumerName = "Zenith Enterprise",
                accountNo = "408129482",
                operator = "NESCO Postpaid",
                billingMonth = "June 2026",
                amount = 3240.00,
                contactNo = "01919887766",
                billNo = "NESC-11428-S"
            ),
            SampleBill(
                consumerName = "Kazi Nazrul Islam",
                accountNo = "507641294",
                operator = "WZPDCO Postpaid",
                billingMonth = "May 2026",
                amount = 780.00,
                contactNo = "01616223344",
                billNo = "WZPD-33291-C"
            )
        )
    }
}

data class SampleBill(
    val consumerName: String,
    val accountNo: String,
    val operator: String,
    val billingMonth: String,
    val amount: Double,
    val contactNo: String,
    val billNo: String
)
