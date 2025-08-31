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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.mcxross.ksui.android.ui.component.GradientButton
import xyz.mcxross.ksui.android.ui.component.SuipTextField
import xyz.mcxross.ksui.android.ui.theme.SuipPink

@Composable
fun SignUpContent(onCreateClick: (fullName: String, email: String) -> Unit) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }

  Column(
    modifier =
      Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 32.dp, vertical = 50.dp)
  ) {
    Spacer(modifier = Modifier.height(64.dp))

    Text(
      text = "SUIP",
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Bold,
      color = SuipPink,
    )

    Text(
      text = "Create your account",
      style = MaterialTheme.typography.headlineMedium,
      color = Color.White,
      modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
    )

    SuipTextField(label = "Your Name", value = fullName, onValueChange = { fullName = it })
    Spacer(modifier = Modifier.height(16.dp))
    SuipTextField(label = "Your Email", value = email, onValueChange = { email = it })
    Spacer(modifier = Modifier.height(24.dp))

    PasskeyInfoCardDark()

    Spacer(modifier = Modifier.height(32.dp))

    TermsAndConditionsText()
    Spacer(modifier = Modifier.height(16.dp))

    GradientButton(text = "Create Account", onClick = { onCreateClick(fullName, email) })
  }
}

@Composable
fun TermsAndConditionsText() {
  Text(
    text =
      buildAnnotatedString {
        append("By creating an account, you agree to our ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = SuipPink)) {
          append("Terms")
        }
        append(" and ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = SuipPink)) {
          append("Privacy Policy")
        }
      },
    textAlign = TextAlign.Center,
    fontSize = 12.sp,
    color = Color.Gray,
  )
}

@Composable
fun PasskeyInfoCardDark() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Signing in",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
        color = Color.White,
      )
      val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.LightGray)) {
          append(
            "A passkey is a faster and safer way to sign in than a password. Your account is created with one unless you choose another option. "
          )
        }
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = SuipPink)) {
          append("How passkeys work")
        }
      }
      Text(text = annotatedString, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
      Text(
        text = "Other ways to sign in",
        color = SuipPink,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}
