package com.personalbiography

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.personalbiography.ui.chat.ChatScreen
import com.personalbiography.ui.settings.SettingsScreen
import com.personalbiography.ui.theme.PersonalBiographyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalBiographyTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "chat") {
                        composable("chat") {
                            ChatScreen(
                                onOpenSettings = { nav.navigate("settings") },
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onBack = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
