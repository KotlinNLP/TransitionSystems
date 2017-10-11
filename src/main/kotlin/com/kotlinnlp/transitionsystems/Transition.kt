/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.transitionsystems

/**
 * The State Transition.
 *
 * @property state the [State] on which this transition operates.
 */
abstract class Transition<SelfType: Transition<SelfType, StateType>, StateType: State<StateType>>(
  val state: StateType
) {

  /**
   * The Transition type.
   *
   * @property direction
   */
  enum class Type(val direction: Deprel.Position) {
    SHIFT(direction = Deprel.Position.NULL),
    RELOCATE(direction = Deprel.Position.NULL),
    WAIT(direction = Deprel.Position.NULL),
    UNSHIFT(direction = Deprel.Position.NULL),
    NO_ARC(direction = Deprel.Position.NULL),
    ARC_LEFT(direction = Deprel.Position.LEFT),
    ARC_RIGHT(direction = Deprel.Position.RIGHT),
    ROOT(direction = Deprel.Position.ROOT)
  }

  /**
   * The Action.
   *
   * Transition abstraction that allows you to ignore which transition-system you are using.
   */
  inner abstract class Action internal constructor(val id: Int = -1, var score: Double) {

    /**
     * @return the state on which this action operates.
     */
    val state: StateType = this@Transition.state

    /**
     * @return the [Transition] from which this [Action] originated.
     */
    @Suppress("UNCHECKED_CAST")
    val transition: SelfType = this@Transition as SelfType

    /**
     * @return the state modified by this [Action]
     */
    fun apply(): StateType {

      val state = this@Transition.apply()

      this.perform(state) // call after the transition has been performed

      return state
    }

    /**
     * @param state the state on which to apply this action
     */
    abstract protected fun perform(state: StateType)
  }

  /**
   * Shift Action.
   */
  inner class Shift internal constructor(id: Int, score: Double) : Action(id, score) {

    /**
     * @param state the state on which to apply this action.
     */
    override fun perform(state: StateType) = Unit

    /**
     * @return its string representation.
     */
    override fun toString(): String = "shift"
  }

  /**
   * Unshift Action.
   */
  inner class Unshift internal constructor(id: Int, score: Double) : Action(id, score) {

    /**
     * @param state the state on which to apply this action.
     */
    override fun perform(state: StateType) = Unit

    /**
     * @return its string representation.
     */
    override fun toString(): String = "unshift"
  }

  /**
   * Relocate Action (can be used to abstract the Swap transition).
   */
  inner class Relocate internal constructor(id: Int, score: Double) : Action(id, score) {

    /**
     * @param state the state on which to apply this action.
     */
    override fun perform(state: StateType) = Unit

    /**
     * @return its string representation.
     */
    override fun toString(): String = "relocate"
  }

  /**
   * NoArc Action.
   */
  inner class NoArc internal constructor(id: Int, score: Double) : Action(id, score) {

    /**
     * @param state the state on which to apply this action.
     */
    override fun perform(state: StateType) = Unit

    /**
     * @return its string representation.
     */
    override fun toString(): String = "no-arc"
  }

  /**
   * Arc Action.
   */
  inner class Arc internal constructor(id: Int, score: Double) : Action(id, score), DependencyRelation {

    /**
     * The dependent id.
     */
    override val dependentId: Int

    /**
     * The governor id (can be null in case the governor is the root).
     */
    override val governorId: Int?

    /**
     * The Dependency Relation (can be null)
     */
    override var deprel: Deprel? = null

    /**
     * Initialize the action.
     */
    init {
      require(this@Transition is SyntacticDependency)

      this@Transition as SyntacticDependency

      this.dependentId = this@Transition.dependentId
      this.governorId = this@Transition.governorId
    }

    /**
     * @param state the state on which to apply this action
     */
    override fun perform(state: StateType) {
      state.dependencyTree.setArc(
        dependentId = this.dependentId,
        governorId = this.governorId,
        deprel = deprel)
    }

    /**
     * @return its string representation.
     */
    override fun toString(): String = "${deprel?:"arc"}(${this.governorId} -> ${this.dependentId})"
  }

  /**
   * The Transition type, from which depends the building of the related [Action].
   */
  abstract val type: Type

  /**
   * The priority of the transition in case of spurious-ambiguities.
   */
  abstract val priority: Int

  /**
   *  True if the transition is allowed in the given parser state.
   */
  abstract val isAllowed: Boolean get

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Action] tied to this transition.
   */
  fun actionFactory(id: Int = -1, score: Double = 0.0, deprel: Deprel? = null): Action {

    val action: Action = this.buildAction(id, score)

    if (this is SyntacticDependency) {
      require(action is DependencyRelation) { "An arc Transition must be associated to a DependencyRelation" }
      (action as DependencyRelation).deprel = deprel
    }

    return action
  }

  /**
   * Apply this transition on a given [state].
   *
   * @return the modified state
   */
  internal fun apply(): StateType {

    this.perform()

    return this.state
  }

  /**
   * Apply this transition on its [state].
   * It requires that the transition [isAllowed] on its [state].
   */
  abstract protected fun perform()

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Action] tied to this transition.
   */
  private fun buildAction(id: Int = -1, score: Double = 0.0): Action = when (this.type) {
    Type.ARC_LEFT, Type.ARC_RIGHT, Type.ROOT -> this.buildArc(id, score)
    Type.NO_ARC -> this.buildNoArc(id, score)
    Type.RELOCATE -> this.buildRelocate(id, score)
    Type.SHIFT, Type.WAIT -> this.buildShift(id, score)
    Type.UNSHIFT -> this.buildUnshift(id, score)
  }

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Shift] tied to this transition.
   */
  private fun buildShift(id: Int = -1, score: Double = 0.0) = this.Shift(id = id, score = score)

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Unshift] tied to this transition.
   */
  private fun buildUnshift(id: Int = -1, score: Double = 0.0) = this.Unshift(id = id, score = score)

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Relocate] tied to this transition.
   */
  private fun buildRelocate(id: Int = -1, score: Double = 0.0) = this.Relocate(id = id, score = score)

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Arc] tied to this transition.
   */
  private fun buildNoArc(id: Int = -1, score: Double = 0.0) = this.NoArc(id = id, score = score)

  /**
   * @param id the id of the action
   * @param score the score of the action
   *
   * @return a new [Arc] tied to this transition.
   */
  private fun buildArc(id: Int = -1, score: Double = 0.0) = this.Arc(id = id, score = score)
}
