package com.cigcounter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val settings by viewModel.settings.collectAsState()
            CigCounterTheme(darkTheme = settings.isDarkMode) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
