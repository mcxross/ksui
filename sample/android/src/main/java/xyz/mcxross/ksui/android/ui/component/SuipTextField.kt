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
package xyz.mcxross.ksui.android.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.mcxross.ksui.android.ui.theme.SuipPurple

@Composable
fun SuipTextField(label: String, value: String, onValueChange: (String) -> Unit) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    singleLine = true,
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SuipPurple,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = SuipPurple,
        unfocusedLabelColor = Color.Gray,
        cursorColor = SuipPurple,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
      ),
  )
}
