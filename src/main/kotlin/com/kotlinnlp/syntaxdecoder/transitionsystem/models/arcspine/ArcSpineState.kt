/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.syntaxdecoder.transitionsystem.models.arcspine

import com.kotlinnlp.syntaxdecoder.transitionsystem.state.State
import com.kotlinnlp.syntaxdecoder.utils.removeFrom

/**
 * The ArcSpine State.
 *
 * @property itemIds the list of item ids used to initialize the state
 */
class ArcSpineState(itemIds: List<Int>) : State<ArcSpineState>(itemIds) {

  /**
   * StackElement.
   *
   * A StackElement contains the root, the left spine and the right spine.
   *
   * @property root the root of this tree.
   * @constructor constructs the left and right spines given the root element.
   */
  data class StackElement(val root: Int) {
    /**
     * The left spine.
     */
    val leftSpine: MutableList<Int> = mutableListOf(root)

    /**
     * The right spine.
     */
    val rightSpine: MutableList<Int> = mutableListOf(root)

    /**
     * @param k the index from which to add the [elements].
     * @param elements elements to add.
     *
     * @return this [StackElement].
     */
    fun addToLeftSpine(k: Int, elements: MutableList<Int>): StackElement {
      this.leftSpine.removeFrom(k + 1).addAll(elements)
      return this
    }

    /**
     * @param k the index from which to add the [elements].
     * @param elements elements to add.
     *
     * @return this [StackElement].
     */
    fun addToRightSpine(k: Int, elements: MutableList<Int>): StackElement {
      this.rightSpine.removeFrom(k + 1).addAll(elements)
      return this
    }

    /**
     * @return its string representation.
     */
    override fun toString(): String {
      return "(${this.leftSpine}, ${this.rightSpine})"
    }
  }

  /**
   * The buffer.
   */
  var buffer: MutableList<Int> = itemIds.toMutableList()

  /**
   * The stack.
   */
  var stack: MutableList<StackElement> = mutableListOf()

  /**
   * True when the state reach the end.
   */
  override val isTerminal get() = this.stack.isEmpty() && this.buffer.isEmpty()

  /**
   * @return a new copy of this [State]
   */
  override fun copy(): ArcSpineState {

    val clonedState = ArcSpineState(this.itemIds)

    clonedState.dependencyTree = this.dependencyTree.clone()
    clonedState.stack = this.stack.toMutableList()

    return clonedState
  }

  /**
   * @return its string representation.
   */
  override fun toString(): String = "S ${this.stack} B ${this.buffer}"
}
