/*
 * Copyright 2024 McXross
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

package xyz.mcxross.ksui.model

import io.ktor.client.statement.*
import xyz.mcxross.ksui.generated.GetAllBalances
import xyz.mcxross.ksui.generated.GetBalance
import xyz.mcxross.ksui.generated.GetCoinMetadata
import xyz.mcxross.ksui.generated.GetCoins
import xyz.mcxross.ksui.generated.GetCommitteeInfo
import xyz.mcxross.ksui.generated.GetDynamicFieldObject
import xyz.mcxross.ksui.generated.GetDynamicFields
import xyz.mcxross.ksui.generated.GetLatestSuiSystemState
import xyz.mcxross.ksui.generated.GetMoveFunctionArgTypes
import xyz.mcxross.ksui.generated.GetObject
import xyz.mcxross.ksui.generated.GetOwnedObjects
import xyz.mcxross.ksui.generated.GetProtocolConfig
import xyz.mcxross.ksui.generated.GetStakes
import xyz.mcxross.ksui.generated.GetStakesByIds
import xyz.mcxross.ksui.generated.GetValidatorsApy
import xyz.mcxross.ksui.generated.QueryTransactionBlocks
import xyz.mcxross.ksui.generated.ResolveNameServiceNames
import xyz.mcxross.ksui.generated.TryGetPastObject

typealias SuiResponse = HttpResponse

typealias CoinMetadata = GetCoinMetadata.Result?

typealias Balances = GetAllBalances.Result?

typealias Balance = GetBalance.Result?

typealias Coins = GetCoins.Result?

typealias CommitteeInfo = GetCommitteeInfo.Result?

typealias Stake = GetStakes.Result?

typealias Stakes = GetStakesByIds.Result?

typealias ProtocolConfig = GetProtocolConfig.Result?

typealias ValidatorsApy = GetValidatorsApy.Result?

typealias LatestSuiSystemState = GetLatestSuiSystemState.Result?

typealias Page = ResolveNameServiceNames.Result?

typealias DynamicFieldObject = GetDynamicFieldObject.Result?

typealias DynamicFields = GetDynamicFields.Result?

typealias Object = GetObject.Result?

typealias OwnedObjects = GetOwnedObjects.Result?

typealias PastObject = TryGetPastObject.Result?

typealias MoveFunctionArgTypes = GetMoveFunctionArgTypes.Result?

typealias TransactionBlocks = QueryTransactionBlocks.Result?
