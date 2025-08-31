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
package xyz.mcxross.ksui.android.model

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import xyz.mcxross.ksui.account.PasskeyAccount
import xyz.mcxross.ksui.core.crypto.PasskeyProvider
import xyz.mcxross.ksui.core.crypto.PasskeyPublicKey
import xyz.mcxross.ksui.model.Result

private val Context.dataStore: DataStore<Preferences> by
  preferencesDataStore(name = "sui_passkey_datastore")

class PasskeyManager(private val context: Context) {

  private val provider = PasskeyProvider(context, "signin.mcxross.xyz")

  var currentAccount: PasskeyAccount? = null
    private set

  private object PrefKeys {
    val USER_PUBLIC_KEY = stringPreferencesKey("user_public_key")
  }

  companion object {
    private const val TAG = "PasskeyManager"
  }

  suspend fun initialize() {
    val keyFromDataStore =
      context.dataStore.data.map { prefs -> prefs[PrefKeys.USER_PUBLIC_KEY] }.firstOrNull()

    if (keyFromDataStore != null) {
      loadAccountFromKey(keyFromDataStore)
      Log.d(TAG, "Passkey Account loaded from DataStore.")
    } else {
      Log.i(TAG, "No saved Passkey Account found to load.")
    }
  }

  private fun loadAccountFromKey(publicKeyString: String) {
    try {
      val publicKeyBytes = Base64.decode(publicKeyString, Base64.URL_SAFE)
      this.currentAccount = PasskeyAccount(PasskeyPublicKey(publicKeyBytes), provider)
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Failed to decode public key", e)
    }
  }

  suspend fun createNewPasskeyAccount(
    activityContext: Context,
    name: String,
    displayName: String,
  ): Boolean {

    val uiProvider = PasskeyProvider(activityContext, "signin.mcxross.xyz")

    return when (val newAccount = PasskeyAccount.create(uiProvider, name, displayName)) {
      is Result.Ok -> {
        saveAccount(newAccount.value)
        true
      }
      is Result.Err -> {
        Log.e(TAG, "Passkey creation failed: ${newAccount.error.message}")
        false
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  suspend fun signIn(activityContext: Context): Boolean {
    if (currentAccount == null) {
      Log.w(TAG, "Cannot sign in. No account has been created or loaded yet.")
      return false
    }

    val accountForSignIn =
      PasskeyAccount(
        currentAccount!!.publicKey,
        PasskeyProvider(activityContext, "signin.mcxross.xyz"),
      )

    return try {
      when (val signatureResult = accountForSignIn.sign(ByteArray(32))) {
        is Result.Ok -> {
          if (signatureResult.value.isNotEmpty()) {
            Log.d(TAG, "Successfully signed in!")
            true
          } else {
            Log.i(TAG, "Passkey sign in was cancelled or failed to produce a signature.")
            false
          }
        }
        is Result.Err -> {
          Log.e(TAG, "Passkey sign in failed: ${signatureResult.error.message}")
          false
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Passkey sign in failed with an exception", e)
      false
    }
  }

  private suspend fun saveAccount(account: PasskeyAccount) {
    val publicKeyString = Base64.encodeToString(account.publicKey.data, Base64.URL_SAFE)
    savePublicKeyToDataStore(publicKeyString)
    this.currentAccount = account
  }

  private suspend fun savePublicKeyToDataStore(publicKeyString: String) {
    context.dataStore.edit { preferences ->
      preferences[PrefKeys.USER_PUBLIC_KEY] = publicKeyString
    }
  }
}
