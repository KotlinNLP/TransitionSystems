/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.transitionsystems.runners

import com.kotlinnlp.transitionsystems.OracleFactory
import com.kotlinnlp.transitionsystems.Transition
import com.kotlinnlp.transitionsystems.TransitionSystem
import com.kotlinnlp.transitionsystems.helpers.ActionsGenerator
import com.kotlinnlp.transitionsystems.helpers.BestActionSelector
import com.kotlinnlp.transitionsystems.helpers.actionsscorer.ActionsErrorsSetter
import com.kotlinnlp.transitionsystems.helpers.actionsscorer.ActionsScorerTrainable
import com.kotlinnlp.transitionsystems.helpers.actionsscorer.features.Features
import com.kotlinnlp.transitionsystems.state.stateview.StateView
import com.kotlinnlp.transitionsystems.state.DecodingContext
import com.kotlinnlp.transitionsystems.state.ExtendedState
import com.kotlinnlp.transitionsystems.state.State
import com.kotlinnlp.transitionsystems.state.items.ItemsFactory
import com.kotlinnlp.transitionsystems.state.items.StateItem
import com.kotlinnlp.transitionsystems.syntax.DependencyTree

/**
 * The helper to train a SyntaxDecoder.
 *
 */
class ActionsScorerTrainer<
  StateType : State<StateType>,
  TransitionType : Transition<TransitionType, StateType>,
  in StateViewType : StateView,
  ContextType : DecodingContext<ContextType>,
  out FeaturesType : Features<*, *>,
  ItemType : StateItem<ItemType, *, *>,
  out ExtendedStateType : ExtendedState<StateType, TransitionType, ItemType, ContextType>>
(
  private val transitionSystem: TransitionSystem<StateType, TransitionType>,
  private val itemsFactory: ItemsFactory<ItemType>,
  private val actionsGenerator: ActionsGenerator<StateType, TransitionType>,
  private val actionsScorer: ActionsScorerTrainable<
    StateType, TransitionType, StateViewType, ContextType, FeaturesType, ItemType, ExtendedStateType>,
  private val actionsErrorsSetter: ActionsErrorsSetter<
    StateType, TransitionType, ItemType, ContextType, ExtendedStateType>,
  private val bestActionSelector: BestActionSelector<
    StateType, TransitionType, ItemType, ContextType, ExtendedStateType>,
  private val oracleFactory: OracleFactory<StateType, TransitionType>
) {

  /**
   * Learn from a single example composed by a list of items and the expected gold [DependencyTree].
   * The best local action is applied with a greedy approach until the final state is reached.
   * Before applying it, the temporary result is compared to the gold [DependencyTree] to
   *
   * @param itemIds a list of item ids
   * @param context a generic [DecodingContext] used to decode
   * @param goldDependencyTree the gold [DependencyTree]
   * @param propagateToInput a Boolean indicating whether errors must be propagated to the input
   * @param beforeApplyAction callback called before applying the best action (default = null)
   *
   * @return the [DependencyTree] built following a greedy approach
   */
  fun learn(itemIds: List<Int>,
            context: ContextType,
            goldDependencyTree: DependencyTree,
            propagateToInput: Boolean,
            beforeApplyAction: ((action: Transition<TransitionType, StateType>.Action,
                                 extendedState: ExtendedStateType) -> Unit)? = null): DependencyTree {

    val state: StateType = this.transitionSystem.getInitialState(itemIds)

    @Suppress("UNCHECKED_CAST")
    val extendedState: ExtendedStateType = ExtendedState(
      state = state,
      items = itemIds.map { id -> this.itemsFactory(id) },
      context = context,
      oracle = this.oracleFactory(goldDependencyTree)) as ExtendedStateType

    while (!state.isTerminal) {

      this.actionsScorer.newExample()

      this.processState(
        extendedState = extendedState,
        propagateToInput = propagateToInput,
        beforeApplyAction = beforeApplyAction)
    }

    return state.dependencyTree
  }

  /**
   * Forward the system processing a state, calculate and propagate errors, apply the best action.
   *
   * @param extendedState the [ExtendedState] context of the state
   * @param propagateToInput a Boolean indicating whether errors must be propagated to the input
   * @param beforeApplyAction callback called before applying the best action (default = null)
   */
  private fun processState(extendedState: ExtendedStateType,
                           propagateToInput: Boolean,
                           beforeApplyAction: ((action: Transition<TransitionType, StateType>.Action,
                                                extendedState: ExtendedStateType) -> Unit)?) {

    val actions = this.actionsGenerator.generateFrom(
      transitions = this.transitionSystem.generateTransitions(extendedState.state))

    this.actionsScorer.score(actions = actions, extendedState = extendedState)

    this.actionsErrorsSetter.assignErrors(actions = actions, extendedState = extendedState)

    if (this.actionsErrorsSetter.areErrorsRelevant) {
      this.actionsScorer.backward(propagateToInput = propagateToInput)
    }

    val bestAction: Transition<TransitionType, StateType>.Action
      = this.bestActionSelector.select(actions = actions, extendedState = extendedState)

    if (beforeApplyAction != null) beforeApplyAction(bestAction, extendedState)

    extendedState.oracle!!.updateWith(bestAction.transition)

    bestAction.apply()
  }
}
