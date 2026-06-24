package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.BillPayment
import com.example.data.repository.SampleBill
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.EnergizingGold
import com.example.ui.theme.FlameRed
import com.example.ui.theme.TealAccent
import com.example.viewmodel.BillViewModel
import com.example.viewmodel.PaymentUiState
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillPayDashboard(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val walletBalance by viewModel.walletBalance.collectAsState()
    val paymentHistory by viewModel.paymentHistory.collectAsState()
    val paymentUiState by viewModel.paymentUiState.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var showRechargeDialog by remember { mutableStateOf(false) }

    // Display Toast notification on payment updates
    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            // Branded Application Header
            AppHeader(
                walletBalance = walletBalance,
                onRechargeClick = { showRechargeDialog = true }
            )

            // Dynamic Navigation Tabs
            val tabs = listOf("Manual Payment", "Sample Bills", "Payment History")
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> ManualPaymentForm(viewModel = viewModel)
                    1 -> SampleBillsList(
                        sampleBills = viewModel.sampleBills,
                        onPayClick = { sampleBill -> viewModel.paySampleBill(sampleBill) }
                    )
                    2 -> PaymentHistoryList(
                        payments = paymentHistory,
                        onPaymentClick = { payment -> viewModel.activeReceiptToShow = payment },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            }
        }

        // Secure Payment Processing Screen Overlay
        if (paymentUiState is PaymentUiState.Processing) {
            ProcessingOverlay()
        }

        // Active Receipt Dialog (Slid up or centered dialog overlay)
        viewModel.activeReceiptToShow?.let { receipt ->
            ReceiptDialog(
                receipt = receipt,
                onDismiss = {
                    viewModel.activeReceiptToShow = null
                    viewModel.dismissPaymentDialog()
                }
            )
        }

        // Wallet Balance Recharge Dialog
        if (showRechargeDialog) {
            RechargeDialog(
                onDismiss = { showRechargeDialog = false },
                onRechargeConfirm = { amount ->
                    viewModel.rechargeWallet(amount)
                    showRechargeDialog = false
                    Toast.makeText(context, "Wallet Recharged: +৳${decimalFormat.format(amount)} BDT", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Handle error notifications gracefully
        if (paymentUiState is PaymentUiState.Error) {
            val errorMsg = (paymentUiState as PaymentUiState.Error).message
            ErrorBanner(
                message = errorMsg,
                onDismiss = { viewModel.dismissPaymentDialog() }
            )
        }
    }
}

@Composable
fun AppHeader(
    walletBalance: Double,
    onRechargeClick: () -> Unit
) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BangladeshGreen,
                            TealAccent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular App Logo with elegant golden border
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, EnergizingGold, CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.priyo_logo),
                        contentDescription = "Priyo Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Priyo Enterprise",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FF66))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Biddut Bill Pay Utility",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick agent portal profile tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EnergizingGold.copy(alpha = 0.15f))
                        .border(1.dp, EnergizingGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AGENT",
                        color = EnergizingGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wallet Balance Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AGENT WALLET BALANCE",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ ${decimalFormat.format(walletBalance)} BDT",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("wallet_balance")
                    )
                }

                // Add balance button
                Button(
                    onClick = onRechargeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EnergizingGold,
                        contentColor = Color(0xFF111111)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("recharge_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Add,
                            contentDescription = "Add Money",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Balance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManualPaymentForm(
    viewModel: BillViewModel
) {
    var expandedOperator by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }

    val operators = listOf(
        "Palli Bidyut (BREB)",
        "DPDC Postpaid",
        "DPDC Prepaid",
        "DESCO Postpaid",
        "DESCO Prepaid",
        "NESCO Postpaid",
        "WZPDCO Postpaid"
    )

    val months = listOf(
        "June 2026",
        "May 2026",
        "April 2026",
        "March 2026",
        "February 2026",
        "January 2026"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Manual Bill Submission",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter the consumer biddut details carefully to generate a manual receipt voucher.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Horizontal Biddut Operator Quick-Selection Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Biddut Provider",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(operators) { op ->
                        val isSelected = viewModel.selectedOperator == op
                        val opAbbr = when {
                            op.contains("Palli") -> "PB"
                            op.contains("DPDC Post") -> "DPDC"
                            op.contains("DPDC Pre") -> "DPDC-P"
                            op.contains("DESCO Post") -> "DESC"
                            op.contains("DESCO Pre") -> "DESC-P"
                            op.contains("NESCO") -> "NESC"
                            else -> "WZPD"
                        }

                        val opColor = when {
                            op.contains("Palli") -> BangladeshGreen
                            op.contains("DPDC") -> Color(0xFF00ACC1)
                            op.contains("DESCO") -> Color(0xFF43A047)
                            op.contains("NESCO") -> Color(0xFFD81B60)
                            else -> Color(0xFFFB8C00)
                        }

                        Card(
                            modifier = Modifier
                                .clickable { viewModel.updateManualOperator(op) }
                                .width(96.dp)
                                .height(68.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) {
                                borderStroke(2.dp, opColor)
                            } else {
                                borderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) opColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(opColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opAbbr.take(2).uppercase(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = opAbbr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelected) opColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Fields (Forms)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Consumer Name
                OutlinedTextField(
                    value = viewModel.consumerName,
                    onValueChange = { viewModel.consumerName = it },
                    label = { Text("Consumer Name") },
                    placeholder = { Text("e.g. Abul Kalam Azad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consumer_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                // Account / Meter No.
                OutlinedTextField(
                    value = viewModel.accountNo,
                    onValueChange = { viewModel.accountNo = it },
                    label = { Text("Account or Meter Number") },
                    placeholder = { Text("e.g. 1023456789") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_no_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                // Billing Month Selector (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.selectedMonth,
                        onValueChange = {},
                        label = { Text("Billing Month") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedMonth = !expandedMonth }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Month"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMonth = !expandedMonth },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        months.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(text = m) },
                                onClick = {
                                    viewModel.updateManualMonth(m)
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                // Bill Amount (BDT)
                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = { viewModel.amountText = it },
                    label = { Text("Bill Amount (৳ BDT)") },
                    placeholder = { Text("e.g. 1250.00") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                // Contact No (SMS Notification)
                OutlinedTextField(
                    value = viewModel.contactNo,
                    onValueChange = { viewModel.contactNo = it },
                    label = { Text("Contact Number (SMS)") },
                    placeholder = { Text("e.g. 01712345678") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_no_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Form Validation Errors
        viewModel.formError?.let { err ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FlameRed.copy(alpha = 0.08f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = "Error icon",
                        tint = FlameRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = err,
                        color = FlameRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Pay Button
        item {
            Button(
                onClick = { viewModel.payManualBill() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_manual_payment"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BangladeshGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "PROCESS MANUAL BILL PAYMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun SampleBillsList(
    sampleBills: List<SampleBill>,
    onPayClick: (SampleBill) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pre-loaded Sample Bills",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Quickly test utility processing using predefined realistic electricity accounts.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        items(sampleBills) { bill ->
            val decimalFormat = remember { DecimalFormat("#,##0.00") }
            val opColor = when {
                bill.operator.contains("Palli") -> BangladeshGreen
                bill.operator.contains("DPDC") -> Color(0xFF00ACC1)
                bill.operator.contains("DESCO") -> Color(0xFF43A047)
                bill.operator.contains("NESCO") -> Color(0xFFD81B60)
                else -> Color(0xFFFB8C00)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Circle Icon representation of operator
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(opColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bill.operator.take(2).uppercase(),
                            color = opColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center bill information details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bill.consumerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bill.operator} • A/C ${bill.accountNo}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Bill Month: ${bill.billingMonth} | Ref: ${bill.billNo}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right column: amount + instant pay trigger
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "৳${decimalFormat.format(bill.amount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = BangladeshGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onPayClick(bill) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BangladeshGreen.copy(alpha = 0.12f),
                                contentColor = BangladeshGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("pay_sample_${bill.accountNo}")
                        ) {
                            Text(
                                text = "Quick Pay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryList(
    payments: List<BillPayment>,
    onPaymentClick: (BillPayment) -> Unit,
    onClearHistory: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    // Filter list of payments based on search criteria
    val filteredPayments = remember(payments, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            payments
        } else {
            payments.filter {
                it.consumerName.contains(searchQuery, ignoreCase = true) ||
                        it.accountNo.contains(searchQuery, ignoreCase = true) ||
                        it.trxId.contains(searchQuery, ignoreCase = true) ||
                        it.operator.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Calculate sum statistics
    val completedPayments = payments.filter { it.status == "Completed" }
    val totalVolume = completedPayments.sumOf { it.amount }
    val totalCount = completedPayments.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Statistical summary panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL COMPLETED VOLUME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "৳ ${decimalFormat.format(totalVolume)} BDT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BILLS PROCESSED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$totalCount Accounts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Search Bar & Clear history trigger row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, account, operator...") },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )

                if (payments.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = FlameRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Empty State Placeholder
        if (filteredPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = borderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Info,
                            contentDescription = "No payments",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching payments found." else "No payment records found.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try redefining your search keywords." else "Completed and manual payment transactions will appear here.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render List items
        items(filteredPayments) { payment ->
            val isSuccess = payment.status == "Completed"
            val statusColor = if (isSuccess) BangladeshGreen else FlameRed
            val opColor = when {
                payment.operator.contains("Palli") -> BangladeshGreen
                payment.operator.contains("DPDC") -> Color(0xFF00ACC1)
                payment.operator.contains("DESCO") -> Color(0xFF43A047)
                payment.operator.contains("NESCO") -> Color(0xFFD81B60)
                else -> Color(0xFFFB8C00)
            }

            val formatter = remember { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) }
            val formattedDate = remember(payment.timestamp) { formatter.format(Date(payment.timestamp)) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPaymentClick(payment) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(opColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = payment.operator.take(2).uppercase(),
                            color = opColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = payment.consumerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Small type tag (Manual vs Sample)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = payment.paymentType.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${payment.operator} • A/C ${payment.accountNo}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$formattedDate | Trx: ${payment.trxId}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "৳${decimalFormat.format(payment.amount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isSuccess) BangladeshGreen else FlameRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Status capsule badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = payment.status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .blur(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = BangladeshGreen,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Processing Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Validating operator gateway and authorizing agent wallet balance deduction...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    receipt: BillPayment,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(receipt.timestamp) { formatter.format(Date(receipt.timestamp)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branded Receipt Header
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BangladeshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Priyo Enterprise",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Biddut Bill Receipt",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BangladeshGreen
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dotted Ticket Divider line
                DottedDivider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(20.dp))

                // Detailed Receipt Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReceiptRow(label = "Consumer Name", value = receipt.consumerName)
                    ReceiptRow(label = "Biddut Provider", value = receipt.operator)
                    ReceiptRow(label = "Account / Meter No", value = receipt.accountNo)
                    ReceiptRow(label = "Billing Month", value = receipt.billingMonth)
                    ReceiptRow(label = "Paid Contact", value = receipt.contactNo)
                    ReceiptRow(label = "Payment Mode", value = "${receipt.paymentType} Submission")
                    ReceiptRow(label = "Processed Date", value = formattedDate)

                    Divider(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Transaction Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (receipt.status == "Completed") BangladeshGreen.copy(alpha = 0.1f)
                                    else FlameRed.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = receipt.status.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (receipt.status == "Completed") BangladeshGreen else FlameRed
                            )
                        }
                    }

                    // Total amount section (Large & clear)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Paid Amount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "৳ ${decimalFormat.format(receipt.amount)} BDT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BangladeshGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction Reference ID with quick Copy block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TRANSACTION REFERENCE ID",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = receipt.trxId,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.testTag("receipt_transaction_id")
                                )
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(receipt.trxId))
                                    Toast.makeText(context, "Copied TrxID: ${receipt.trxId}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BangladeshGreen.copy(alpha = 0.1f),
                                    contentColor = BangladeshGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Realistic simulated printable barcode lines
                SimulatedBarcode()

                Spacer(modifier = Modifier.height(24.dp))

                // Dismiss Receipt Card
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_receipt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "DONE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DottedDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Gray.copy(alpha = 0.4f), Color.Transparent)
                )
            )
    )
}

@Composable
fun SimulatedBarcode() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(0.8f)
                .background(Color.White),
            horizontalArrangement = Arrangement.Center
        ) {
            // Create a fake barcode with a series of black vertical bars of varying widths
            val barWidths = listOf(1, 3, 2, 1, 4, 1, 2, 3, 1, 1, 2, 4, 1, 3, 1, 2, 2, 1, 4, 1, 2, 1)
            barWidths.forEachIndexed { index, width ->
                Box(
                    modifier = Modifier
                        .width(width.dp)
                        .fillMaxHeight()
                        .background(if (index % 2 == 0) Color.Black else Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "* PRIYO-ENTERPRISE-PAY *",
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun RechargeDialog(
    onDismiss: () -> Unit,
    onRechargeConfirm: (Double) -> Unit
) {
    var rechargeText by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    val amounts = listOf(500.0, 1000.0, 2000.0, 5000.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recharge Agent Wallet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulate adding funds to your enterprise account instantly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of preselected quick amounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amounts.forEach { amt ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    rechargeText = amt.toInt().toString()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (rechargeText == amt.toInt().toString()) {
                                    BangladeshGreen.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = borderStroke(
                                1.dp,
                                if (rechargeText == amt.toInt().toString()) BangladeshGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "৳${amt.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rechargeText == amt.toInt().toString()) BangladeshGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text field for custom input
                OutlinedTextField(
                    value = rechargeText,
                    onValueChange = {
                        rechargeText = it
                        inputError = null
                    },
                    label = { Text("Enter Recharge Amount (৳ BDT)") },
                    placeholder = { Text("e.g. 2500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recharge_amount_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                inputError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = err,
                        color = FlameRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "CANCEL")
                    }

                    Button(
                        onClick = {
                            val amt = rechargeText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                inputError = "Enter a valid recharge amount"
                            } else {
                                onRechargeConfirm(amt)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_recharge"),
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "RECHARGE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FlameRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = FlameRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Transaction Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("error_dialog_dismiss"),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "DISMISS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Compact helper to build BorderStroke to prevent custom class clashes
@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return remember(width, color) {
        androidx.compose.foundation.BorderStroke(width, color)
    }
}
