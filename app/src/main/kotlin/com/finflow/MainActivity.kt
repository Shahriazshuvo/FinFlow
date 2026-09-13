package com.finflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finflow.core.designsystem.theme.FinFlowTheme
import com.finflow.ui.FinFlowApp
import com.finflow.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

            FinFlowTheme {
                FinFlowApp(sessionState = sessionState)
            }
        }
    }
}