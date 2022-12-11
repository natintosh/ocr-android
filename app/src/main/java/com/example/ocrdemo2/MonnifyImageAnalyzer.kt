package com.example.ocrdemo2

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@ExperimentalGetImage
class MonnifyImageAnalyzer : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { text ->
                    Log.d(SecondFragment::javaClass.name, "Text Scanned: ${text.text}")
                }
                .addOnFailureListener { task ->
                    Log.d(SecondFragment::javaClass.name, "ImageAnalyzer: ${task.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}