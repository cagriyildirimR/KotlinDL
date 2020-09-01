package examples.custom

import api.keras.Sequential
import api.keras.activations.Activations
import api.keras.callbacks.Callback
import api.keras.dataset.Dataset
import api.keras.history.*
import api.keras.initializers.Constant
import api.keras.initializers.HeNormal
import api.keras.initializers.Zeros
import api.keras.layers.Dense
import api.keras.layers.Flatten
import api.keras.layers.Input
import api.keras.layers.twodim.Conv2D
import api.keras.layers.twodim.ConvPadding
import api.keras.layers.twodim.MaxPool2D
import api.keras.loss.LossFunctions
import api.keras.metric.Metrics
import api.keras.optimizers.SGD
import datasets.*

private const val EPOCHS = 1
private const val TRAINING_BATCH_SIZE = 500
private const val TEST_BATCH_SIZE = 1000
private const val NUM_CHANNELS = 1L
private const val IMAGE_SIZE = 28L
private const val SEED = 12L

/**
 * Kotlin implementation of LeNet on Keras.
 * Architecture could be copied here: https://github.com/TaavishThaman/LeNet-5-with-Keras/blob/master/lenet_5.py
 */
private val model = Sequential.of<Float>(
    Input(
        IMAGE_SIZE,
        IMAGE_SIZE,
        NUM_CHANNELS
    ),
    Conv2D(
        filters = 32,
        kernelSize = longArrayOf(5, 5),
        strides = longArrayOf(1, 1, 1, 1),
        activation = Activations.Relu,
        kernelInitializer = HeNormal(SEED),
        biasInitializer = Zeros(),
        padding = ConvPadding.SAME
    ),
    MaxPool2D(
        poolSize = intArrayOf(1, 2, 2, 1),
        strides = intArrayOf(1, 2, 2, 1)
    ),
    Conv2D(
        filters = 64,
        kernelSize = longArrayOf(5, 5),
        strides = longArrayOf(1, 1, 1, 1),
        activation = Activations.Relu,
        kernelInitializer = HeNormal(SEED),
        biasInitializer = Zeros(),
        padding = ConvPadding.SAME
    ),
    MaxPool2D(
        poolSize = intArrayOf(1, 2, 2, 1),
        strides = intArrayOf(1, 2, 2, 1)
    ),
    Flatten(), // 3136
    Dense(
        outputSize = 512,
        activation = Activations.Relu,
        kernelInitializer = HeNormal(SEED),
        biasInitializer = Constant(0.1f)
    ),
    Dense(
        outputSize = AMOUNT_OF_CLASSES,
        activation = Activations.Linear,
        kernelInitializer = HeNormal(SEED),
        biasInitializer = Constant(0.1f)
    )
)

fun main() {
    val (train, test) = Dataset.createTrainAndTestDatasets(
        TRAIN_IMAGES_ARCHIVE,
        TRAIN_LABELS_ARCHIVE,
        TEST_IMAGES_ARCHIVE,
        TEST_LABELS_ARCHIVE,
        AMOUNT_OF_CLASSES,
        ::extractImages,
        ::extractLabels
    )

    model.use {
        it.compile(
            optimizer = SGD(learningRate = 0.1f),
            loss = LossFunctions.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            callback = CustomCallback()
        )

        println(it.kGraph)

        it.fit(dataset = train, epochs = EPOCHS, batchSize = TRAINING_BATCH_SIZE, verbose = true)

        val accuracy = it.evaluate(dataset = test, batchSize = TEST_BATCH_SIZE).metrics[Metrics.ACCURACY]

        println("Accuracy: $accuracy")
    }
}

class CustomCallback : Callback<Float>() {
    override fun onEpochBegin(epoch: Int, trainingHistory: TrainingHistory) {
        println("Epoch $epoch begins.")
    }

    override fun onEpochEnd(epoch: Int, logs: EpochTrainingEvent) {
        println("Epoch $epoch ends.")
    }

    override fun onTrainBatchBegin(batch: Int, trainingHistory: TrainingHistory) {
        println("Training batch $batch begins.")
    }

    override fun onTrainBatchEnd(batch: Int, logs: BatchTrainingEvent?, trainingHistory: TrainingHistory) {
        println("Training batch $batch ends with loss ${logs!!.lossValue}.")
    }

    override fun onTrainBegin() {
        println("Train begins")
    }

    override fun onTrainEnd(trainingHistory: TrainingHistory) {
        println("Train ends with last loss ${trainingHistory.lastBatchEvent().lossValue}")
    }

    override fun onTestBatchBegin(batch: Int, logs: History) {
        println("Test batch $batch begins.")
    }

    override fun onTestBatchEnd(batch: Int, logs: BatchEvent?, testHistory: History) {
        println("Test batch $batch ends with loss ${logs!!.lossValue}..")
    }

    override fun onTestBegin() {
        println("Test begins")
    }

    override fun onTestEnd(testHistory: History) {
        println("Train ends with last loss ${testHistory.lastBatchEvent().lossValue}")
    }

    override fun onPredictBatchBegin(batch: Int) {
        println("Prediction batch $batch begins.")
    }

    override fun onPredictBatchEnd(batch: Int) {
        println("Prediction batch $batch ends.")
    }

    override fun onPredictBegin() {
        println("Train begins")
    }

    override fun onPredictEnd() {
        println("Test begins")
    }
}