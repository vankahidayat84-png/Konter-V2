package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.ShinfoxApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ShinfoxViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate our robust offline business viewmodel
        val viewModel = ViewModelProvider(this).get(ShinfoxViewModel::class.java)

        setContent {
            MyApplicationTheme {
                ShinfoxApp(viewModel = viewModel)
            }
        }
    }
}
