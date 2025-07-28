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
package xyz.mcxross.ksui.sample

import xyz.mcxross.ksui.account.Account

const val ALICE_PRIVATE_KEY = "suiprivkey1qqtp4ugtv40c6tj4a7r4vd8ft4nykpxsrh07yqssklraxy243us5qyczx9z"
val ALICE_ACCOUNT = Account.import(ALICE_PRIVATE_KEY)

const val BOB_PASS_PHRASE = "oyster recycle orange priority diesel flash turn merit nation wood benefit fall"
val BOB_ACCOUNT = Account.import(BOB_PASS_PHRASE.split(" "))

const val CAROL_PRIVATE_KEY = "suiprivkey1qpx4h5re3myaz9chqtp89szeqf3c865h8ma80vkudn0g6kkj0qpgvw69u8y"
val CAROL_ACCOUNT = Account.import(CAROL_PRIVATE_KEY)
