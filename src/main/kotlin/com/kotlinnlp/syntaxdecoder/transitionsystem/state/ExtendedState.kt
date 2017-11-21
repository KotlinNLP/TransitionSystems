/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.syntaxdecoder.transitionsystem.state

import com.kotlinnlp.syntaxdecoder.context.DecodingContext
import com.kotlinnlp.syntaxdecoder.transitionsystem.oracle.Oracle
import com.kotlinnlp.syntaxdecoder.context.items.StateItem
import com.kotlinnlp.syntaxdecoder.transitionsystem.Transition

/**
 * The [ExtendedState] extends a [State] with the list of [StateItem]s that compose it and a [DecodingContext].
 *
 * This structure allows you to keep aligned with state the properties that can evolve together with it.
 *
 * @property state a [State]
 * @property context a [DecodingContext]
 * @property oracle an [Oracle] (optional)
 */
data class ExtendedState<
  StateType : State<StateType>,
  TransitionType: Transition<TransitionType, StateType>,
  ItemType : StateItem<ItemType, *, *>,
  ContextType : DecodingContext<ContextType, ItemType>>(
  val state: StateType,
  val context: ContextType,
  val oracle: Oracle<StateType, TransitionType>? = null) {

  /**
   * The score of goodness of this state (a value in the range (0.0, 1.0]), default 1.0.
   */
  val score: Double get() = 1 / (-0.1 * this.logScore + 1) // convert the domain (-inf, 0.0] to (0.0, 1.0]

  /**
   * The score of goodness of this state (a value in the range (-inf, 0.0]), default 0.0.
   * It is the result of more additions of the logarithm of scores in the range (0.0, 1.0], done calling the
   * [accumulateScore] method.
   */
  var logScore: Double = 0.0
    private set

  /**
   * Accumulate the given [score] into this state as joint probability of its score (after have transformed it by
   * natural logarithm, to avoid underflow).
   *
   * @param score a score in the range (0.0, 1.0]
   */
  fun accumulateScore(score: Double) {
    assert(score > 0 && score in 0.0 .. 1.0) { "Invalid score: $score, must be in range (0.0, 1.0]." }
    this.logScore += Math.log(score)
  }

  /**
   * @param state the new state that will replace the current one in the clone
   *
   * @return a clone of this [ExtendedState] replacing its state with the given [state]
   */
  fun clone(state: StateType): ExtendedState<StateType, TransitionType, ItemType, ContextType> {

    val clonedState = ExtendedState(
      state = state,
      context = this.context.copy(),
      oracle = this.oracle?.copy())

    clonedState.logScore = this.logScore

    return clonedState
  }
}
