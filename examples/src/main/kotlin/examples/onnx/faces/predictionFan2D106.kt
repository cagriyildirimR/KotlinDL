/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.onnx.faces

import examples.transferlearning.getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.*
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.load
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.resize
import org.jetbrains.kotlinx.dl.visualization.swing.drawLandMarks
import java.io.File

fun main() {
    val modelHub = ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val modelType = ONNXModels.FaceAlignment.Fan2d106
    val model = modelHub.loadModel(modelType)

    model.use {
        println(it)

        for (i in 0..8) {
            val imageFile = getFileFromResource("datasets/faces/image$i.jpg")
            val preprocessing: Preprocessing = preprocess {
                transformImage {
                    load {
                        pathToData = imageFile
                        imageShape = ImageShape(224, 224, 3)
                        colorMode = ColorOrder.BGR
                    }
                    resize {
                        outputHeight = 192
                        outputWidth = 192
                    }
                }
            }

            val inputData = modelType.preprocessInput(preprocessing)

            val yhat = it.predictRaw(inputData)
            println(yhat.toTypedArray().contentDeepToString())

            visualiseLandMarks(imageFile, yhat)
        }
    }
}

fun visualiseLandMarks(
    imageFile: File,
    landmarks: List<Array<*>>
) {
    val preprocessing: Preprocessing = preprocess {
        transformImage {
            load {
                pathToData = imageFile
                imageShape = ImageShape(224, 224, 3)
                colorMode = ColorOrder.BGR
            }
            resize {
                outputWidth = 192
                outputHeight = 192
            }
        }
        transformTensor {
            rescale {
                scalingCoefficient = 255f
            }
        }
    }

    val rawImage = preprocessing().first

    drawLandMarks(rawImage, ImageShape(192, 192, 3), landmarks)
}