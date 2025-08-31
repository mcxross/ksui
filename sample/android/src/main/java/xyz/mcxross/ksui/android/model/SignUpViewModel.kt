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

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.mcxross.ksui.account.PasskeyAccount

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

  private val manager = PasskeyManager(application)

  var currentAccount by mutableStateOf<PasskeyAccount?>(null)
    private set

  init {
    viewModelScope.launch {
      manager.initialize()
      currentAccount = manager.currentAccount
    }
  }

  fun createNewPasskeyAccount(name: String, displayName: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      manager.createNewPasskeyAccount(name, displayName)
      currentAccount = manager.currentAccount
      if (manager.currentAccount != null) {
        onSuccess()
      }
    }
  }

  /** Initiates the sign-in process and invokes the onSuccess callback upon completion. */
  @RequiresApi(Build.VERSION_CODES.O)
  fun signIn(onSuccess: () -> Unit) {
    viewModelScope.launch {
      val signedInSuccessfully = manager.signIn()
      if (signedInSuccessfully) {
        onSuccess()
      }
    }
  }
}
