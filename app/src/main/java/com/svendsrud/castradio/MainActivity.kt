package com.svendsrud.castradio

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.svendsrud.castradio.navigation.CastRadioNavHost
import com.svendsrud.castradio.ui.theme.CastRadioTheme

// AppCompatActivity (FragmentActivity) er påkrevd for at MediaRouteButton
// skal kunne vise enhetsvelger-dialogen sin.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Headeren har alltid en mørk gradient bak statuslinjen, så ikonene skal være lyse.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            CastRadioTheme {
                CastRadioNavHost()
            }
        }
    }
}
