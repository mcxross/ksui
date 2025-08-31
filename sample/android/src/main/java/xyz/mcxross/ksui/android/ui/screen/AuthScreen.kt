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
package xyz.mcxross.ksui.android.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.mcxross.ksui.android.R
import xyz.mcxross.ksui.android.model.SignUpViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpScreen(
  viewModel: SignUpViewModel = viewModel(),
  onSignInSuccess: () -> Unit,
  onSignUpSuccess: () -> Unit,
) {
  val account = viewModel.currentAccount

  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(id = R.drawable.bg),
      contentDescription = "Background",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

    if (account != null) {
      SignInContent(
        address = account.address.toString(),
        onSignInClick = { viewModel.signIn(onSignInSuccess) },
      )
    } else {
      SignUpContent(
        onCreateClick = { name, displayName ->
          viewModel.createNewPasskeyAccount(name, displayName, onSignUpSuccess)
        }
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SignUpContentPreview() {
  MaterialTheme {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(id = R.drawable.bg),
        contentDescription = "Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
      Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
      SignUpContent(onCreateClick = { _, _ -> })
    }
  }
}
