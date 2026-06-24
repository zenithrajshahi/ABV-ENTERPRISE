package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.BillRepository
import com.example.ui.screens.BillPayDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BillViewModel
import com.example.viewmodel.BillViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Offline persistence database & repository
    val database = AppDatabase.getDatabase(this)
    val repository = BillRepository(database.billPaymentDao())
    
    // Initialize core ViewModel with Factory
    val viewModel: BillViewModel by viewModels {
      BillViewModelFactory(repository)
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          BillPayDashboard(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

