package com.example.polihackplm2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.polihackplm2.ui.PhishShieldApp
import com.example.polihackplm2.ui.PhishShieldViewModel
import com.example.polihackplm2.ui.theme.Polihackplm2Theme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT, 
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        val viewModel = ViewModelProvider(this)[PhishShieldViewModel::class.java]
        handleIntent(intent, viewModel)

        setContent {
            Polihackplm2Theme(
                dynamicColor = false
            ) {
                PhishShieldApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val viewModel = ViewModelProvider(this)[PhishShieldViewModel::class.java]
        handleIntent(intent, viewModel)
    }

    private fun handleIntent(intent: Intent?, viewModel: PhishShieldViewModel) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val url = intent.dataString
            if (url != null) {
                val unwrappedUrl = com.example.polihackplm2.functionality.UrlUnwrapper.unwrap(url)
                viewModel.setPendingUrl(unwrappedUrl)
            }
        }
    }
}
