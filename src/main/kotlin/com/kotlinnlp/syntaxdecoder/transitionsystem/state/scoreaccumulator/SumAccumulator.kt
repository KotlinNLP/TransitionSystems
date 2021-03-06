/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.syntaxdecoder.transitionsystem.state.scoreaccumulator

/**
 * The score accumulator that does the average of the accumulations.
 */
class SumAccumulator : ScoreAccumulator<SumAccumulator>() {

  /**
   * The factory of a new [SumAccumulator].
   */
  companion object Factory : ScoreAccumulator.Factory {

    /**
     * Private val used to serialize the class (needed from Serializable)
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L

    /**
     * @return a new score accumulator
     */
    override fun invoke() = SumAccumulator()
  }

  /**
   * @return the current score
   */
  override fun getCurrent(): Double = this.accumulatedScore

  /**
   * Simulate the accumulation of a given score and get the estimated future score.
   *
   * @param addingScore the adding score
   *
   * @return the estimated future score obtained accumulating the given [addingScore]
   */
  override fun estimateAccumulation(addingScore: Double): Double = this.getNextAccumulatedScore(addingScore)

  /**
   * @param addingScore the adding score
   *
   * @return the replacing value of the [accumulatedScore]
   */
  override fun getNextAccumulatedScore(addingScore: Double): Double = this.accumulatedScore + addingScore

  /**
   * @return a copy of this accumulator
   */
  override fun copy(): SumAccumulator {

    val clonedAccumulator = SumAccumulator()

    clonedAccumulator.accumulatedScore = this.accumulatedScore
    clonedAccumulator.accumulations = this.accumulations

    return clonedAccumulator
  }
}
