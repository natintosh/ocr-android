package com.example.ocrdemo2

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ocrdemo2.databinding.FragmentFirstBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) navigateToSecondFragment()
            else showPermissionRationaleAlertDialog(
                permission = android.Manifest.permission.CAMERA,
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            checkPermissionAndAskIfRequired(
                permission = android.Manifest.permission.CAMERA,
                onPermissionGranted = {
                    navigateToSecondFragment()
                },
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToSecondFragment() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    private fun showPermissionRationaleAlertDialog(
        permission: String,
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.allow_monnify_to_access_your_camera))
            .setMessage(resources.getString(R.string.reason_monnify_need_camera_access))
            .setNegativeButton(resources.getString(R.string.deny)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.allow)) { dialog, _ ->
                dialog.dismiss()
                requestPermissionLauncher.launch(permission)
            }.show()
    }

    private fun checkPermissionAndAskIfRequired(
        permission: String,
        onPermissionGranted: () -> Unit,
    ) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> onPermissionGranted.invoke()
            shouldShowRequestPermissionRationale(permission) -> showPermissionRationaleAlertDialog(
                permission = permission,
            )
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

}