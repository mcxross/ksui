package xyz.mcxross.ksui.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import xyz.mcxross.ksui.android.ui.theme.KsuiTheme
import xyz.mcxross.ksui.android.ui.view.Account
import xyz.mcxross.ksui.android.ui.view.Wallet
import xyz.mcxross.sc.SuiCommons
import xyz.mcxross.sc.model.KeyDetails

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      KsuiTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          var created by remember { mutableStateOf(false) }
          var keyDetails by remember { mutableStateOf<KeyDetails?>(null) }
          if (!created) {
            Wallet(
              Modifier.padding(),
              {
                val details = SuiCommons.derive.importKey(it)
                keyDetails = details
                created = true
              },
            ) {
              val details = SuiCommons.derive.newKey()
              keyDetails = details
              created = true
            }
          } else {
            Account(this, keyDetails)
          }
        }
      }
    }
  }
}
