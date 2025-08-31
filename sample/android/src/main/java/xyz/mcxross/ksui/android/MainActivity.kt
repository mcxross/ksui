/*
 * Copyright 2025 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.mcxross.ksui.android.model.SignUpViewModel
import xyz.mcxross.ksui.android.ui.screen.DatingProfileScreen
import xyz.mcxross.ksui.android.ui.screen.SignUpScreen

class MainActivity : ComponentActivity() {
  private val viewModel: SignUpViewModel by viewModels()

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent { AppNavigation(viewModel = viewModel) }
  }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(viewModel: SignUpViewModel) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "signup") {
    composable("signup") {
      SignUpScreen(
        viewModel = viewModel,
        onSignInSuccess = {
          navController.navigate("home") { popUpTo("signup") { inclusive = true } }
        },
        onSignUpSuccess = {
          navController.navigate("home") { popUpTo("signup") { inclusive = true } }
        },
      )
    }

    composable("home") { DatingProfileScreen() }
  }
}
