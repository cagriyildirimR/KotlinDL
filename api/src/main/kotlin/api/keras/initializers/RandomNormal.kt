package api.keras.initializers

import org.tensorflow.Operand
import org.tensorflow.op.Ops

class RandomNormal(
    private val mean: Float,
    private val stdev: Float,
    private val seed: Long
) :
    Initializer() {
    override fun initialize(
        fanIn: Int,
        fanOut: Int,
        tf: Ops,
        shape: Operand<Int>,
        name: String
    ): Operand<Float> {
        val seeds = longArrayOf(seed, 0L)
        val distOp: Operand<Float> = tf.random.statelessRandomNormal(shape, tf.constant(seeds))
        val op: Operand<Float> = tf.math.mul(distOp, tf.constant(stdev))
        return tf.withName(name).math.add(op, tf.constant(mean))
    }

    override fun toString(): String {
        return "RandomNormal(mean=$mean, stdev=$stdev, seed=$seed)"
    }
}