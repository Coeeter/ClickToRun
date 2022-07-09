package com.example.clicktorun.ui.fragments.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.databinding.FragmentPhotoBottomSheetBinding
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.utils.REQUEST_CODE_TO_PICK_IMAGE
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPhotoBottomSheetBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoBottomSheetBinding.inflate(inflater, container, false)
        binding.changeImageBtn.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                REQUEST_CODE_TO_PICK_IMAGE
            )
        }
        binding.deleteImageBtn.setOnClickListener {
            authViewModel.deleteImage()
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TO_PICK_IMAGE &&
            resultCode == Activity.RESULT_OK &&
            data != null &&
            data.data != null
        ) {
            authViewModel.uri = data.data
            authViewModel.updateUser(true)
            findNavController().popBackStack()
        }
    }
}