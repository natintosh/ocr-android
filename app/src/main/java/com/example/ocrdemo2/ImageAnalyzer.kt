package com.example.ocrdemo2

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@ExperimentalGetImage
class ImageAnalyzer : ImageAnalysis.Analyzer {


    companion object {
        private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        private val cardNumberPattern = Regex(CardDetails.CARD_NUMBER_REGEX)
        private val expiryDatePattern = Regex(CardDetails.EXPIRY_DATE_REGEX)
    }


    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)



            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d(SecondFragment::javaClass.name, "Text Scanned: ${visionText.text}")

                    for (textBlock in visionText.textBlocks) {
                        for (line in textBlock.lines) {
                            val text = line.text.replace(Regex("[^\\w]"), "")

                            val cardNumberMatch = cardNumberPattern.find(text)

                            if (cardNumberMatch != null) {
                                Log.d(
                                    SecondFragment::javaClass.name,
                                    "CARD NUMBER: ${cardNumberMatch.value}"
                                )
                            }

                            val expiryDateMatch = expiryDatePattern.find(text)

                            if (expiryDateMatch != null) {
                                Log.d(
                                    SecondFragment::javaClass.name,
                                    "Expiry DATE: ${expiryDateMatch.value}"
                                )
                            }

                        }
                    }
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