/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.syntaxdecoder.modules.actionserrorssetter

import com.kotlinnlp.syntaxdecoder.context.InputContext
import com.kotlinnlp.syntaxdecoder.context.items.StateItem
import com.kotlinnlp.syntaxdecoder.transitionsystem.state.ExtendedState
import com.kotlinnlp.syntaxdecoder.transitionsystem.Transition
import com.kotlinnlp.syntaxdecoder.transitionsystem.oracle.Oracle
import com.kotlinnlp.syntaxdecoder.transitionsystem.state.State


/**
 * The [ActionsErrorsSetter] that implement the hinge-loss used for "maximum-margin" classification.
 *
 * If the margin between the highest correct action score and the highest incorrect action score is greater then the
 * given [learningMarginThreshold] then errors are relevant and they are set into the two actions mentioned above.
 *
 * @property learningMarginThreshold the learning margin threshold
 */
class HingeLossActionsErrorsSetter<
  StateType : State<StateType>,
  TransitionType : Transition<TransitionType, StateType>,
  ItemType : StateItem<ItemType, *, *>,
  InputContextType : InputContext<InputContextType, ItemType>>
(
  private val learningMarginThreshold: Double = 1.0
) : ActionsErrorsSetter<
  StateType,
  TransitionType,
  ItemType,
  InputContextType>() {

  /**
   * Assign errors to the given [sortedActions] using the given [extendedState] as context.
   *
   * @param sortedActions a list with the last scored actions, sorted by score and then by transition priority
   * @param extendedState the extended state of the last scored actions
   */
  override fun assignErrors(sortedActions: List<Transition<TransitionType, StateType>.Action>,
                            extendedState: ExtendedState<StateType, TransitionType, ItemType, InputContextType>) {

    val oracle: Oracle<StateType, TransitionType> = checkNotNull(extendedState.oracle)

    if (sortedActions.size > 1) {

      val highestScoringCorrectAction = sortedActions.first { oracle.isCorrect(it) }
      val highestScoringIncorrectAction = sortedActions.first { !oracle.isCorrect(it) }

      val margin: Double = highestScoringCorrectAction.score - highestScoringIncorrectAction.score

      if (margin < this.learningMarginThreshold) {

        highestScoringCorrectAction.error = -1.0
        highestScoringIncorrectAction.error = 1.0

        this.areErrorsRelevant = true
      }
    }
  }
}
