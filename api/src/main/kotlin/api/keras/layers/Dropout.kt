package api.keras.layers

import api.core.KGraph
import api.keras.util.getDType
import org.tensorflow.Operand
import org.tensorflow.Shape
import org.tensorflow.op.Ops

class Dropout(
    private val keepProbability: Float,
    name: String = ""
) : Layer(name) {

    override fun defineVariables(tf: Ops, kGraph: KGraph, inputShape: Shape) {
        //left empty
    }

    override fun computeOutputShape(inputShape: Shape): Shape {
        return inputShape
    }

    override fun transformInput(tf: Ops, input: Operand<Float>): Operand<Float> {
        val trainingFactor = tf.placeholderWithDefault(tf.constant(0.0f), Shape.scalar())

        val probability = tf.math.add(
            tf.math.mul(trainingFactor, tf.constant(keepProbability - 1.0f)),
            tf.constant(1.0f)
        )// When training

        val inputShape = input.asOutput().shape()
        val dims = mutableListOf<Long>()
        for (i in 1 until inputShape.numDimensions()) // skip first dimension
            dims.add(inputShape.size(i))

        val randomUniform = tf.random.randomUniform(tf.constant(dims.toLongArray()), getDType())

        val mask = tf.math.floor(tf.math.add(randomUniform, probability as Operand<Float>))

        return tf.math.div(tf.math.mul(input, mask), probability)
    }

    override fun getWeights(): List<Array<*>> {
        return emptyList()
    }

    override fun hasActivation(): Boolean {
        return false
    }

    override fun getParams(): Int {
        return 0
    }

    override fun toString(): String {
        return "Dropout(keepProbability=$keepProbability)"
    }
}