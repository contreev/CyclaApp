package com.example.cyclapp.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.Category
import java.nio.MappedByteBuffer

class WasteClassifier(context: Context) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val modelPath = "model.tflite"
    private val labelPath = "labels.txt"

    init {
        try {
            val model: MappedByteBuffer = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            labels = FileUtil.loadLabels(context, labelPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): List<Category> {
        if (interpreter == null || labels.isEmpty()) return emptyList()

        // El modelo de Teachable Machine usa 224x224
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normaliza a [0, 1] que es lo que espera Floating Point de TM
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val outputBuffer = Array(1) { FloatArray(labels.size) }
        interpreter?.run(tensorImage.buffer, outputBuffer)

        val result = mutableListOf<Category>()
        for (i in labels.indices) {
            result.add(Category(labels[i], outputBuffer[0][i]))
        }

        return result.sortedByDescending { it.score }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
