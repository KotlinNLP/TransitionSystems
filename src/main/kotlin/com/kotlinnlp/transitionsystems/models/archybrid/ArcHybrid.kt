/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.transitionsystems.models.archybrid

import com.kotlinnlp.transitionsystems.*
import com.kotlinnlp.transitionsystems.state.templates.StackBufferState
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
   * The [TransitionsGenerator] used to generate the next valid transitions given a [State].
   */
  override val transitionsGenerator = ArcHybridTransitionsGenerator()
}
