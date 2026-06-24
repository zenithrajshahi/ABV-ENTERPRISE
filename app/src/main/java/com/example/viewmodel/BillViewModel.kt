package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BillPayment
import com.example.data.repository.BillRepository
import com.example.data.repository.SampleBill
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

sealed interface PaymentUiState {
    object Idle : PaymentUiState
    object Processing : PaymentUiState
    data class Success(val receipt: BillPayment) : PaymentUiState
    data class Error(val message: String) : PaymentUiState
}

class BillViewModel(private val repository: BillRepository) : ViewModel() {

    // Persistent-like wallet balance in memory, can be recharged
    private val _walletBalance = MutableStateFlow(12500.00)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Payment history from Room Database
    val paymentHistory: StateFlow<List<BillPayment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of predefined Bangladesh electricity sample bills
    val sampleBills: List<SampleBill> = repository.getSamplePendingBills()

    // Active payment UI State
    private val _paymentUiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

    // Manual payment form fields
    var consumerName by mutableStateOf("")
    var accountNo by mutableStateOf("")
    var selectedOperator by mutableStateOf("Palli Bidyut (BREB)")
    var selectedMonth by mutableStateOf("June 2026")
    var amountText by mutableStateOf("")
    var contactNo by mutableStateOf("")

    // Form errors
    var formError by mutableStateOf<String?>(null)

    // Current receipt detail display
    var activeReceiptToShow by mutableStateOf<BillPayment?>(null)

    fun updateManualOperator(operator: String) {
        selectedOperator = operator
    }

    fun updateManualMonth(month: String) {
        selectedMonth = month
    }

    fun resetForm() {
        consumerName = ""
        accountNo = ""
        amountText = ""
        contactNo = ""
        formError = null
    }

    fun rechargeWallet(amount: Double) {
        viewModelScope.launch {
            _walletBalance.value += amount
        }
    }

    fun paySampleBill(sampleBill: SampleBill) {
        viewModelScope.launch {
            _paymentUiState.value = PaymentUiState.Processing
            delay(1500) // Realistic server delay simulation

            val currentBalance = _walletBalance.value
            if (currentBalance < sampleBill.amount) {
                _paymentUiState.value = PaymentUiState.Error("Insufficient balance in your Priyo Enterprise wallet. Please recharge.")
                return@launch
            }

            // Deduct balance
            _walletBalance.value -= sampleBill.amount

            // Generate Txn ID
            val trxId = generateTrxId()

            val completedPayment = BillPayment(
                consumerName = sampleBill.consumerName,
                accountNo = sampleBill.accountNo,
                operator = sampleBill.operator,
                billingMonth = sampleBill.billingMonth,
                amount = sampleBill.amount,
                contactNo = sampleBill.contactNo,
                trxId = trxId,
                status = "Completed",
                paymentType = "Sample"
            )

            repository.insertPayment(completedPayment)
            _paymentUiState.value = PaymentUiState.Success(completedPayment)
            activeReceiptToShow = completedPayment
        }
    }

    fun payManualBill() {
        // Validate
        if (consumerName.trim().isEmpty()) {
            formError = "Please enter consumer name"
            return
        }
        if (accountNo.trim().isEmpty()) {
            formError = "Please enter account or meter number"
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            formError = "Please enter a valid amount greater than 0"
            return
        }
        if (contactNo.trim().isEmpty() || contactNo.length < 11) {
            formError = "Please enter a valid 11-digit mobile number"
            return
        }

        formError = null

        viewModelScope.launch {
            _paymentUiState.value = PaymentUiState.Processing
            delay(1800) // Simulation delay

            val currentBalance = _walletBalance.value
            if (currentBalance < amount) {
                // Record a failed transaction for manual payment to make it realistic!
                val trxId = generateTrxId()
                val failedPayment = BillPayment(
                    consumerName = consumerName.trim(),
                    accountNo = accountNo.trim(),
                    operator = selectedOperator,
                    billingMonth = selectedMonth,
                    amount = amount,
                    contactNo = contactNo.trim(),
                    trxId = trxId,
                    status = "Failed",
                    paymentType = "Manual"
                )
                repository.insertPayment(failedPayment)

                _paymentUiState.value = PaymentUiState.Error("Payment Failed: Insufficient Wallet Balance.")
                return@launch
            }

            // Deduct balance
            _walletBalance.value -= amount

            // Generate Txn ID
            val trxId = generateTrxId()

            val completedPayment = BillPayment(
                consumerName = consumerName.trim(),
                accountNo = accountNo.trim(),
                operator = selectedOperator,
                billingMonth = selectedMonth,
                amount = amount,
                contactNo = contactNo.trim(),
                trxId = trxId,
                status = "Completed",
                paymentType = "Manual"
            )

            repository.insertPayment(completedPayment)
            _paymentUiState.value = PaymentUiState.Success(completedPayment)
            activeReceiptToShow = completedPayment
            resetForm()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllPayments()
        }
    }

    fun dismissPaymentDialog() {
        _paymentUiState.value = PaymentUiState.Idle
    }

    private fun generateTrxId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = StringBuilder("PRIYO")
        for (i in 0..6) {
            code.append(chars[Random.nextInt(chars.length)])
        }
        return code.toString()
    }
}

class BillViewModelFactory(private val repository: BillRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
