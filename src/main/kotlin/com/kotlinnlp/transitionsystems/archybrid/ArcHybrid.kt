/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.transitionsystems.archybrid

import com.kotlinnlp.transitionsystems.*
import com.kotlinnlp.transitionsystems.archybrid.transitions.*
import kotlin.reflect.KClass

/**
 * The ArcHybrid Transition System (Kuhlmann et al., 2011).
 */
class ArcHybrid : TransitionSystem<StackBufferState, ArcHybridTransition>() {

  /**
   * The [KClass] of the StateType used in the [getInitialState] function.
   */
  override val stateClass: KClass<StackBufferState> = StackBufferState::class

  /**
   * @param state the state from which to extract valid transitions.
   *
   * @return a list of valid transitions for the given [state].
   */
  override fun getValidTransitions(state: StackBufferState): List<ArcHybridTransition> {
    return listOf(
      Root(state),
      Shift(state),
      ArcLeft(state),
      ArcRight(state)
    ).filter { it.isAllowed }
  }
}
