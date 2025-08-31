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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.mcxross.ksui.android.R
import xyz.mcxross.ksui.android.ui.component.GradientButton
import xyz.mcxross.ksui.android.ui.theme.SuipPink

@Composable
fun SignInContent(address: String, onSignInClick: () -> Unit) {
  Column(
    modifier =
      Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 32.dp, vertical = 50.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = "Welcome Back!",
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Bold,
      color = Color.White,
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = address,
      style = MaterialTheme.typography.bodyLarge,
      color = SuipPink,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(48.dp))
    GradientButton(text = "Sign In with Passkey", onClick = onSignInClick)
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SignInContentPreview() {
  MaterialTheme {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(id = R.drawable.bg),
        contentDescription = "Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
      Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
      SignInContent(address = "0x1234...5678", onSignInClick = {})
    }
  }
}
