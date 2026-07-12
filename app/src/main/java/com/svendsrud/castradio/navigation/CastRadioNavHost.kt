package com.svendsrud.castradio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svendsrud.castradio.ui.RadioScreen
import kotlinx.serialization.Serializable

@Serializable
data object StationsRoute

@Composable
fun CastRadioNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = StationsRoute) {
        composable<StationsRoute> {
            RadioScreen()
        }
    }
}
