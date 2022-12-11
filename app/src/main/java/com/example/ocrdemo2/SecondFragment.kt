package com.example.ocrdemo2

import android.os.Bundle
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ocrdemo2.databinding.FragmentSecondBinding
import com.google.common.util.concurrent.ListenableFuture
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


        val imageAnalyzer = ImageAnalysis.Builder().build()

        imageAnalyzer.setAnalyzer(executor, MonnifyImageAnalyzer())


        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageAnalyzer)
            .setViewPort(binding.cameraPreviewView.viewPort!!)
            .build()

        val camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            useCaseGroup
        )

        setupCameraFlash(camera)
        setupTouchFocus(camera)
    }

    private fun setupCameraFlash(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            binding.cameraFlashButton.visibility = View.VISIBLE
            binding.cameraFlashButton.setOnClickListener {
                camera.cameraControl.enableTorch(camera.cameraInfo.torchState.value == TorchState.OFF)
            }
            camera.cameraInfo.torchState.observe(viewLifecycleOwner) {
                binding.cameraFlashButton.text =
                    getText(if (it == TorchState.ON) R.string.flash_on else R.string.flash_off)
            }
        }
    }

    private fun setupTouchFocus(camera: Camera) {


        binding.cameraPreviewView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.performClick()
                return@setOnTouchListener false
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = SurfaceOrientedMeteringPointFactory(
                    view.measuredWidth.toFloat(),
                    view.measuredHeight.toFloat()
                )
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point).build()
                camera.cameraControl.startFocusAndMetering(action)
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }
    }
}
