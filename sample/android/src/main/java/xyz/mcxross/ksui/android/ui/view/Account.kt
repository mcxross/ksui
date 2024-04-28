package xyz.mcxross.ksui.android.ui.view

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*
import kotlinx.coroutines.launch
import xyz.mcxross.ksui.android.controller.send
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.util.requestTestTokens
import xyz.mcxross.sc.model.KeyDetails

@Composable
fun Account(context: Context, keyDetails: KeyDetails?) {

  val clipboardManager: ClipboardManager = LocalClipboardManager.current

  var balance = remember { mutableLongStateOf(0L) }
  val coroutineScope = rememberCoroutineScope()
  val suiHttpClient = remember { suiHttpClient { endpoint = EndPoint.DEVNET } }
  // This state is used to trigger balance.graphql updates.
  val updateTrigger = remember { mutableIntStateOf(0) }

  val isBlurred = remember { mutableStateOf(true) }

  val prepSending = remember { mutableStateOf(false) }

  val recipient = remember { mutableStateOf("") }

  LaunchedEffect(key1 = updateTrigger.intValue) {
    balance.longValue = suiHttpClient.getBalance(SuiAddress(keyDetails?.address ?: "")).totalBalance
  }

  // Periodic balance.graphql update logic
  DisposableEffect(Unit) {
    val timer = Timer()
    timer.schedule(
      object : TimerTask() {
        override fun run() {
          coroutineScope.launch {
            // Increment the trigger to refresh the balance.graphql
            updateTrigger.intValue += 1
          }
        }
      },
      0,
      5000,
    ) // Update every 5 seconds

    onDispose { timer.cancel() }
  }

  Column {
    Text("Account", fontWeight = FontWeight.Bold)
    Divider(modifier = Modifier.padding(5.dp))

    Text(
      text = keyDetails?.address ?: "",
      modifier =
        Modifier.padding(4.dp).clickable {
          clipboardManager.setText(AnnotatedString(keyDetails?.address ?: ""))
        },
    )

    Text(
      "Balance: ${balance.longValue.div(1_000_000_000)} SUI",
      modifier = Modifier.padding(4.dp),
      fontWeight = FontWeight.Bold,
    )

    Text(
      text = keyDetails?.phrase ?: "",
      modifier =
        Modifier.padding(4.dp)
          .then(if (isBlurred.value) Modifier.blur(5.dp) else Modifier)
          .clickable { isBlurred.value = !isBlurred.value },
    )

    if (!isBlurred.value) {
      Button(
        onClick = { clipboardManager.setText(AnnotatedString(keyDetails?.phrase ?: "")) },
        modifier = Modifier.padding(5.dp),
      ) {
        Text("Copy Phrase")
      }
    }

    if (prepSending.value) {
      TextField(
        value = recipient.value,
        onValueChange = { recipient.value = it },
        label = { Text("Recipient Address") },
        modifier = Modifier.fillMaxWidth().padding(5.dp),
      )
    }

    // Don't show send btn if user has no balance.graphql
    if (balance.longValue > 0) {
      Button(
        onClick = {
          if (prepSending.value && recipient.value.isNotEmpty()) {
            coroutineScope.launch {
              send(
                keyDetails?.sk ?: "",
                SuiAddress(keyDetails?.address ?: ""),
                SuiAddress(recipient.value),
              )
            }
          }
          prepSending.value = !prepSending.value
        },
        modifier = Modifier.fillMaxWidth().padding(5.dp),
        shape = RoundedCornerShape(15.dp),
      ) {
        if (prepSending.value && recipient.value.isEmpty()) {
          Text("Cancel")
        } else {
          Text("Send")
        }
      }
    }

    Button(
      onClick = {
        coroutineScope.launch {
          if (keyDetails != null) {
            try {
              suiHttpClient.requestTestTokens(SuiAddress(keyDetails.address))
            } catch (e: Exception) {
              Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
          } else {
            Toast.makeText(null, "No key details found, please create a wallet", Toast.LENGTH_SHORT)
              .show()
          }
        }
      },
      modifier = Modifier.fillMaxWidth().padding(5.dp),
      shape = RoundedCornerShape(15.dp),
    ) {
      Text("Request Test Tokens")
    }
  }
}
