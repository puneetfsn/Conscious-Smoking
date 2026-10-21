package com.cigcounter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.cigcounter.app.ui.CigViewModel
import com.cigcounter.app.ui.MainScreen
import com.cigcounter.app.ui.theme.CigCounterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CigViewModel by viewModels {
        CigViewModel.Factory(application as CigApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CigCounterTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
