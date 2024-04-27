package xyz.mcxross.ksui.android.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Wallet(
  modifier: Modifier = Modifier,
  onImportWallet: (String) -> Unit,
  onNewWallet: () -> Unit,
) {
  var importing by remember { mutableStateOf(false) }
  var importString by remember { mutableStateOf("") }
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Column {
      if (!importing) {
        Button(modifier = Modifier.padding(8.dp).fillMaxWidth(), onClick = onNewWallet) {
          Text("Create Wallet")
        }
      } else {
        TextField(
          modifier = Modifier.fillMaxWidth(),
          value = importString,
          label = { Text("Enter Phrase or Private Key") },
          onValueChange = { importString = it },
        )
      }
      Button(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        onClick = {
          if (importing) {
            onImportWallet(importString)
          }
          importing = true
        },
      ) {
        Text("Import Wallet")
      }
    }
  }
}
