/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.api.core.loss

import org.jetbrains.kotlinx.dl.api.core.shape.TensorShape
import org.junit.jupiter.api.Test
import org.tensorflow.EagerSession
import org.tensorflow.Operand
import org.tensorflow.op.Ops
import kotlin.test.assertEquals

internal class BinaryCrossEntropyTest {
    @Test
    fun basic() {
        val yTrueArray = floatArrayOf(1f, 0f, 1f, 0f)
        val yPredArray = floatArrayOf(1f, 1f, 1f, 0f)

        EagerSession.create().use { session ->
            val tf = Ops.create(session)
            val instance = BinaryCrossEntropy(reductionType = ReductionType.SUM_OVER_BATCH_SIZE)

            val yTrue: Operand<Float> = tf.reshape(tf.constant(yTrueArray), tf.constant(intArrayOf(2, 2)))
            val yPred: Operand<Float> = tf.reshape(tf.constant(yPredArray), tf.constant(intArrayOf(2, 2)))

            val numberOfLosses = tf.constant(TensorShape(yPred.asOutput().shape()).numElements().toFloat())

            assertEquals(4f, numberOfLosses.asOutput().tensor().floatValue())

            val operand: Operand<Float> = instance.apply(tf, yPred = yPred, yTrue = yTrue, numberOfLosses)

            assertEquals(
                3.8333097f,
                operand.asOutput().tensor().floatValue()
            )
        }
    }

    @Test
    fun sumReduction() {
        val yTrueArray = floatArrayOf(1f, 0f, 1f, 0f)
        val yPredArray = floatArrayOf(1f, 1f, 1f, 0f)

        EagerSession.create().use { session ->
            val tf = Ops.create(session)
            val instance = BinaryCrossEntropy(reductionType = ReductionType.SUM)

            val yTrue: Operand<Float> = tf.reshape(tf.constant(yTrueArray), tf.constant(intArrayOf(2, 2)))
            val yPred: Operand<Float> = tf.reshape(tf.constant(yPredArray), tf.constant(intArrayOf(2, 2)))

            val operand: Operand<Float> = instance.apply(tf, yPred = yPred, yTrue = yTrue, null)

            assertEquals(
                7.6666193f,
                operand.asOutput().tensor().floatValue()
            )
        }
    }
}
