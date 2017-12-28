/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.syntaxdecoder.transitionsystem.models.arcrelocate

import com.kotlinnlp.syntaxdecoder.transitionsystem.state.templates.StackBufferState
import com.kotlinnlp.syntaxdecoder.transitionsystem.TransitionSystem
import kotlin.reflect.KClass

/**
 * The ArcRelocate Transition System (Grella, 2011).
 *
 * The ArcRelocate parsing is a form of shift-reduce parsing where arc-creations actions happen between
 * the top two elements on the stack.
 */
class ArcRelocate : TransitionSystem<StackBufferState, ArcRelocateTransition>() {

  /**
   * The [KClass] of the StateType used in the [getInitialState] function.
   */
  override val stateClass: KClass<StackBufferState> = StackBufferState::class

  /**
   * The TransitionsGenerator used to generate the next valid transitions given a [State].
   */
  override val transitionsGenerator = ArcRelocateTransitionsGenerator()
}
