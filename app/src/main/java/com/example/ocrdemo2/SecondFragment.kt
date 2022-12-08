package com.example.ocrdemo2

import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.Toast
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

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        _binding = FragmentSecondBinding.inflate(inflater, container, false)




        return binding.root
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun  onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    @androidx.camera.core.ExperimentalGetImage
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()

        val cameraSelector: CameraSelector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)


        val cardFrameWidth = binding.cameraCardFrameView.measuredWidth
        val cardFrameHeight = binding.cameraCardFrameView.measuredHeight;

        val viewport = ViewPort.Builder(Rational(cardFrameWidth, cardFrameHeight), rotation)
            .build()


        val textRecognition = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//
//        val mlkit = MlKitAnalyzer(
//            listOf(textRecognition),
//            COORDINATE_SYSTEM_VIEW_REFERENCED,
//            ContextCompat.getMainExecutor(requireContext())
//        ) {
//
//            Log.d("QRCodeAnalyzer", "Barcode scanned: ${it.getValue(textRecognition)}")
//            Toast.makeText(
//                requireContext(),
//                "Tag Added: ${it.getValue(textRecognition)}",
//                Toast.LENGTH_SHORT
//            ).show()
//
//        }
//
        val imageAnalyzer = ImageAnalysis.Builder().build().also { imageAnalysis ->
            imageAnalysis.setAnalyzer(executor) {
                val image = it.image
                if (image != null) {
                    val inputImage = InputImage.fromMediaImage(image, rotation)
                    val task = textRecognition.process(inputImage).addOnSuccessListener { text ->


                        Log.d("TextRecognition", "Text scanned: ${text.textBlocks}")
                        Toast.makeText(
                            requireContext(),
                            "Tag Added: ${text.text}",
                            Toast.LENGTH_SHORT
                        ).show()
                        image.close()
                    }
                }
            }
        }


        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageAnalyzer)
            .setViewPort(viewport)
            .build()

        var camera =
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, useCaseGroup)

    }
}