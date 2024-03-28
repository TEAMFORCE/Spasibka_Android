package com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.BuildConfig
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateReportBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CreateReportViewModel
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_ID
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.getFilePathFromUri
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class CreateReportFragment : BaseFragment<FragmentCreateReportBinding>(FragmentCreateReportBinding::inflate) {


    private val viewModel: CreateReportViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showDialogCameraOrGallery()
            } else {
                showDialogAboutPermissions()
            }
        }

    private var imageFilePart: MultipartBody.Part? = null
    private var filePath: String? = null
    private var idChallenge: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            idChallenge = getInt(CHALLENGER_ID)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        idChallenge?.let {
            restoreSavedDateFromSP(challengeId = it)
            saveWithoutSending(it)
            deleteReportStateWhenFieldsAreNull(it)
            listenersSuccessResponseOfCreateReport(it)
        }
        attachDetachImageInPreview()
        listenersErrorCreateChallenge()
        isLoading()
        binding.closeBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.sendReport.setOnClickListener {
            if (isAccessOrNoForSendReportBtn())
                sendReport(idChallenge!!)
            else{
                Toast.makeText(requireContext(), R.string.mustBePhotoOrDescription, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        binding.commentValueEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.sendReport.isEnabled = (isAccessOrNoForSendReportBtn())
            }
        })

        binding.sendReport.isEnabled = false

    }

    override fun applyTheme() {
      //  binding.sendReport.setThemeColor(appTheme)
    }

    private fun isAccessOrNoForSendReportBtn(): Boolean =
        (binding.commentValueEt.text.trim().isNotEmpty() || imageFilePart != null) && idChallenge != null


    private fun deleteReportStateWhenFieldsAreNull(challengeId: Int) {
        if (binding.commentValueEt.text.trim().isEmpty() ||
            filePath.isNullOrEmpty()
        ) {
            deleteReportState(challengeId)
        }
    }

    private fun saveWithoutSending(challengeId: Int) {
        binding.saveWithoutSendingBtn.setOnClickListener {
            if (binding.commentValueEt.text.trim().isNotEmpty() ||
                !filePath.isNullOrEmpty()
            ) {
                saveReportState(challengeId)
            }
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun sendReport(challengeId: Int) {
        val comment = binding.commentValueEt.text.toString()
        val image = imageFilePart
        viewModel.createReport(challengeId, comment, image)
    }

    private fun listenersSuccessResponseOfCreateReport(challengeId: Int) {
        viewModel.isSuccessOperation.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.successCreateReport),
                    Toast.LENGTH_LONG
                ).show()
                clearFields()
                deleteReportState(challengeId)
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun isLoading() {
        viewModel.isLoading.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    binding.sendReport.isEnabled = false
                    binding.progressBar.visible()
                } else {
                    binding.progressBar.invisible()
                    binding.sendReport.isEnabled = true
                }

            }
        )
    }

    private fun clearFields() {
        binding.commentValueEt.setText("")
        binding.showAttachedImgCard.invisible()
        imageFilePart = null
        filePath = null
    }

    private fun listenersErrorCreateChallenge() {
        viewModel.createReportError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreSavedDateFromSP(challengeId: Int) {
        val sharedPref = requireContext().getSharedPreferences("report${challengeId}", 0)
        if (!sharedPref.getString("commentReport${challengeId}", "").isNullOrEmpty()) {
            binding.commentValueEt.setText(
                sharedPref.getString("commentReport${challengeId}", "")
            )
        }
        if (!sharedPref.getString("imageReport${challengeId}", "").isNullOrEmpty()) {
            filePath = sharedPref.getString("imageReport${challengeId}", "")
            binding.showAttachedImgCard.visibility = View.VISIBLE
            Glide.with(this)
                .load(filePath)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.image)
            uriToMultipart(filePath!!)
        }
    }

    private fun saveReportState(challengeId: Int) {
        if (binding.commentValueEt.text.trim().isNotEmpty() || !filePath.isNullOrEmpty()) {
            val sharedPref =
                requireContext().getSharedPreferences("report${idChallenge}", 0)
            val editor = sharedPref.edit()
            editor.putString("commentReport${challengeId}", binding.commentValueEt.text.toString())
            editor.putString("imageReport${challengeId}", filePath)
            editor.apply()
        } else {
            deleteReportState(challengeId)
        }

        // При восстановление нужно прогнать filePath через uriToMultipart
    }

    private fun deleteReportState(challengeId: Int) {
        requireContext().getSharedPreferences("report${challengeId}", 0).edit().clear().apply()
    }


    private fun attachDetachImageInPreview() {
        binding.attachImageBtn.setOnClickListener {
            showPhoneStatePermission()
        }

        binding.detachImgBtn.setOnClickListener {
            binding.showAttachedImgCard.visibility = View.GONE
            imageFilePart = null
            filePath = null
        }
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val path = result.data?.data?.let { getFilePathFromUri(requireContext(), it, true) }
                if (path != null) {
                    binding.showAttachedImgCard.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(path)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.image)
                    uriToMultipart(path)
                }
            }
        }

    private var latestTmpUri: Uri? = null
    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let {
                    binding.showAttachedImgCard.visible()
                    Glide.with(requireContext())
                        .load(it)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.image)
                    val path = getFilePathFromUri(requireContext(), it, true)
                    uriToMultipart(path)
                }
            }
        }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri? {
        val cacheDir: File? = activity?.cacheDir
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return activity?.applicationContext?.let {
            FileProvider.getUriForFile(
                it,
                "${BuildConfig.APPLICATION_ID}.provider", tmpFile
            )
        }
    }

    private fun showDialogCameraOrGallery() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.whatApproachToGetImage))
            .setNegativeButton(resources.getString(R.string.gallery)) { dialog, _ ->
                getImageFromGallery()
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.camera)) { dialog, _ ->
                takeImage()
                dialog.cancel()
            }
            .show()
    }

    private fun showPhoneStatePermission() {

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                showDialogCameraOrGallery()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRequestPermissionRational()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showDialogAboutPermissions() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissions))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.settings)) { dialog, which ->
                dialog.cancel()
                val reqIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply {
                        val uri = Uri.fromParts("package", "com.teamforce.thanksapp", null)
                        data = uri
                    }
                startActivity(reqIntent)
                // Почему то повторно не запрашивается разрешение
                // requestPermissions()
            }
            .show()
    }

    private fun showRequestPermissionRational() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissionsRational))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.good)) { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                dialog.cancel()
            }
            .show()
    }

    private fun getImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent.type = "image/*"
        resultLauncher.launch(pickIntent)
    }


    private fun uriToMultipart(filePathInner: String) {
        filePath = filePathInner
        val file = File(filePathInner)
        val requestFile: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        imageFilePart = body
    }

    override fun onStop() {
        super.onStop()
        setFragmentResult(CREATE_REPORT_REQUEST_KEY, Bundle().apply { putBoolean(
            CREATE_REPORT_DRAFT_UPDATED, true) })

    }

    companion object {

        const val CREATE_REPORT_REQUEST_KEY = "Create Report Request Key"
        const val CREATE_REPORT_DRAFT_UPDATED = "Create Report Draft Updated"

        @JvmStatic
        fun newInstance(challengeId: Int) =
            CreateReportFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHALLENGER_ID, challengeId)
                }
            }
    }
}