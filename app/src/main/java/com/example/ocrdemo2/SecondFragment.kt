package com.example.ocrdemo2

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.ocrdemo2.databinding.FragmentSecondBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

@androidx.camera.core.ExperimentalGetImage
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var rotation = Surface.ROTATION_0

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        return binding.root
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
        }

        val cameraSelector: CameraSelector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()


        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


        val imageAnalyzer = ImageAnalysis.Builder().build()

        imageAnalyzer.setAnalyzer(executor) { imageProxy ->

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


        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageAnalyzer)
            .setViewPort(binding.cameraPreviewView.viewPort!!)
            .build()

        val camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            useCaseGroup
        )

        if (camera.cameraInfo.hasFlashUnit()) {
            binding.cameraFlashButton.setOnClickListener {
                camera.cameraControl.enableTorch(camera.cameraInfo.torchState.value == TorchState.OFF)
            }
        }
    }
}