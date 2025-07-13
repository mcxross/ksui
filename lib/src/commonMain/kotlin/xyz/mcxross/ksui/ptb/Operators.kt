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
package xyz.mcxross.ksui.ptb


/**
 * Defines the unary plus operator for a single `Argument`.
 *
 * This allows you to start an argument list with a single argument using the `+` prefix,
 * which is a highly idiomatic way to begin a collection in a DSL.
 *
 * @return A new `List<Argument>` containing just this single argument.
 */
operator fun Argument.unaryPlus(): List<Argument> {
  return listOf(this)
}


/**
 * Defines the `+` operator for combining two `Argument` objects into a `List<Argument>`.
 *
 * This is the starting point for creating a fluent, chainable syntax for building
 * argument lists for a `moveCall`. Once a list is created, Kotlin's standard
 * library `plus` operator can be used to append additional arguments.
 *
 * @param other The `Argument` to add to the right-hand side.
 * @return A new `List<Argument>` containing both arguments.
 */
operator fun Argument.plus(other: Argument): List<Argument> {
  return listOf(this, other)
}

