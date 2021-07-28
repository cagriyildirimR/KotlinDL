/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.api.core.layer.core

import org.jetbrains.kotlinx.dl.api.core.KGraph
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.HeNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.HeUniform
import org.jetbrains.kotlinx.dl.api.core.initializer.Initializer
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.regularizer.Regularizer
import org.jetbrains.kotlinx.dl.api.core.shape.TensorShape
import org.jetbrains.kotlinx.dl.api.core.shape.numElements
import org.jetbrains.kotlinx.dl.api.core.util.denseBiasVarName
import org.jetbrains.kotlinx.dl.api.core.util.denseKernelVarName
import org.jetbrains.kotlinx.dl.api.core.util.getDType
import org.tensorflow.Operand
import org.tensorflow.Shape
import org.tensorflow.op.Ops
import org.tensorflow.op.core.Variable

private const val KERNEL_VARIABLE_NAME = "dense_kernel"
private const val BIAS_VARIABLE_NAME = "dense_bias"

/**
 * Densely-connected (fully-connected) layer class.
 *
 * This layer implements the operation:
 * `outputs = activation(inputs * kernel + bias)`
 *
 * where `activation` is the element-wise activation function
 * passed as the `activation` argument, `kernel` is a weights matrix
 * created by the layer, and `bias` is a bias vector created by the layer
 * (only applicable if `use_bias` is `True`).
 *
 * @property [outputSize] Dimensionality of the output space.
 * @property [activation] Activation function.
 * @property [kernelInitializer] Initializer function for the 'kernel' weights matrix.
 * @property [biasInitializer] Initializer function for the bias.
 * @property [kernelRegularizer] Regularizer function applied to the `kernel` weights matrix.
 * @property [biasRegularizer] Regularizer function applied to the `bias` vector.
 * @property [activityRegularizer] Regularizer function applied to the output of the layer (its "activation").
 * @property [useBias] If true the layer uses a bias vector.
 * @property [name] Custom layer name.
 * @constructor Creates [Dense] object.
 */
public class Dense(
    public val outputSize: Int = 128,
    public val activation: Activations = Activations.Relu,
    public val kernelInitializer: Initializer = HeNormal(),
    public val biasInitializer: Initializer = HeUniform(),
    public val kernelRegularizer: Regularizer? = null,
    public val biasRegularizer: Regularizer? = null,
    public val activityRegularizer: Regularizer? = null,
    public val useBias: Boolean = true,
    name: String = ""
) : Layer(name) {
    private lateinit var kernelShape: Shape
    private lateinit var biasShape: Shape

    // weight tensors
    private lateinit var kernel: Variable<Float>
    private lateinit var bias: Variable<Float>

    override fun build(tf: Ops, kGraph: KGraph, inputShape: Shape) {
        // Compute shapes of kernel and bias matrices
        kernelShape = Shape.make(inputShape.size(inputShape.numDimensions() - 1), outputSize.toLong())
        biasShape = Shape.make(outputSize.toLong())

        fanIn = inputShape.size(inputShape.numDimensions() - 1).toInt()
        fanOut = outputSize

        val (kernelVariableName, biasVariableName) = defineVariableNames()
        createDenseVariables(tf, kernelVariableName, biasVariableName, kGraph)
    }

    private fun defineVariableNames(): Pair<String, String> {
        return if (name.isNotEmpty()) {
            Pair(denseKernelVarName(name), denseBiasVarName(name))
        } else {
            Pair(KERNEL_VARIABLE_NAME, BIAS_VARIABLE_NAME)
        }
    }

    private fun createDenseVariables(
        tf: Ops,
        kernelVariableName: String,
        biasVariableName: String,
        kGraph: KGraph
    ) {
        kernel = tf.withName(kernelVariableName).variable(kernelShape, getDType())
        if (useBias) bias = tf.withName(biasVariableName).variable(biasShape, getDType())

        kernel = addWeight(tf, kGraph, kernelVariableName, kernel, kernelInitializer, kernelRegularizer)
        if (useBias) bias = addWeight(tf, kGraph, biasVariableName, bias, biasInitializer, biasRegularizer)
    }

    override fun computeOutputShape(inputShape: Shape): Shape {
        return TensorShape(inputShape).replaceLast(outputSize.toLong()).toShape()
    }

    override fun forward(
        tf: Ops,
        input: Operand<Float>,
        isTraining: Operand<Boolean>,
        numberOfLosses: Operand<Float>?
    ): Operand<Float> {
        val signal: Operand<Float> = tf.math.add(tf.linalg.matMul(input, kernel), bias)
        return Activations.convert(activation).apply(tf, signal, name)
    }

    override var weights: Map<String, Array<*>>
        get() = extractDenseWeights()
        set(value) = assignWeights(value)

    private fun extractDenseWeights(): Map<String, Array<*>> {
        return extractWeights(defineVariableNames().toList())
    }

    override val hasActivation: Boolean get() = true

    override val paramCount: Int
        get() = (kernelShape.numElements() + biasShape.numElements()).toInt()

    /** Returns the shape of kernel weights. */
    public val kernelShapeArray: LongArray get() = TensorShape(kernelShape).dims()

    /** Returns the shape of bias weights. */
    public val biasShapeArray: LongArray get() = TensorShape(biasShape).dims()

    override fun toString(): String {
        return "Dense(outputSize=$outputSize, activation=$activation, kernelInitializer=$kernelInitializer, biasInitializer=$biasInitializer, kernelShape=$kernelShape, biasShape=$biasShape)"
    }
}