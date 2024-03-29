package com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentMyResultChallengeBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.ResultChallengeAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_ID
import com.teamforce.thanksapp.presentation.viewmodel.challenge.MyResultChallengeViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ResultsChallengeViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.showImageWithBaseUrlWithOpportunityToDownload
import com.teamforce.thanksapp.utils.Extensions.showImageWithOpportunityToDownload
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.glide.setImageFromStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class MyResultChallengeFragment : BaseFragment<FragmentMyResultChallengeBinding>(FragmentMyResultChallengeBinding::inflate) {

    private val viewModel: MyResultChallengeViewModel by viewModels()
    private val resultChallengeViewModel: ResultsChallengeViewModel by activityViewModels()

    private var idChallenge: Int? = null
    private var listAdapter: ResultChallengeAdapter? = null
    private var textDraft: String? = null
    private var imageDraft: String? = null


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showDialogAboutPermissions()

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
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.cancel()
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idChallenge = it.getInt(CHALLENGER_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        idChallenge?.let {
            checkExistingDraft(it)
            loadMyResult(it)
        }

        listeners()

        resultChallengeViewModel.needUpdateDraft.observe(viewLifecycleOwner){
            if(it && idChallenge != null){
                checkExistingDraft(idChallenge!!)
            }
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

    }


    override fun applyTheme() {

    }

    private fun checkExistingDraft(challengeId: Int) {
        if(hasReportDraft(challengeId)){
            binding.draftFrame.visible()
            setDraftData(challengeId)
        }else{
            binding.draftFrame.invisible()
        }

        binding.editDraftBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putInt(CHALLENGER_ID, challengeId)
            }
            it.findNavController().navigateSafely(
                R.id.action_global_createReportFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.publicDraftBtn.setOnClickListener {
            // TODO Отправка отчета челленджа
            // TODO По результату запроса нужно удалить черновик и обновить список моих результатов
            blockBtn()
            sendReport(challengeId)
        }
    }

    // TODO Описать общий класс с нужными функциями для обработки изображений, или написать хотя бы функцию общую
    private fun uriToMultipart(filePathInner: String): MultipartBody.Part {
        val file = File(filePathInner)
        val requestFile: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("photo", file.name, requestFile)
    }

    private fun sendReport(challengeId: Int) {
        val comment = binding.draftTextTv.text.toString()
        val image = uriToMultipart(imageDraft ?: "")
        viewModel.createReport(challengeId, comment, image)
    }

    private fun hasReportDraft(challengeId: Int): Boolean {
        val sharedPref = requireContext().getSharedPreferences("report${challengeId}", 0)
        return (!sharedPref.getString("commentReport${challengeId}", "").isNullOrEmpty() ||
                !sharedPref.getString("imageReport${challengeId}", "").isNullOrEmpty())

    }

    private fun setDraftData(challengeId: Int){
        val sharedPref = requireContext().getSharedPreferences("report${challengeId}", 0)
        textDraft = sharedPref.getString("commentReport${challengeId}", "")
        imageDraft = sharedPref.getString("imageReport${challengeId}", "")
        if (!imageDraft.isNullOrEmpty()){
            binding.draftImageSiv.visible()
            binding.draftImageSiv.setImageFromStorage(imageDraft!!)
            draftPhotoListener(imageDraft!!)
        }else{
            binding.draftImageSiv.invisible()
        }
        binding.draftTextTv.text = textDraft
    }

    private fun draftPhotoListener(photo: String){
        binding.draftImageSiv.setOnClickListener { clickedView ->
            (clickedView as ImageView).showImageWithBaseUrlWithOpportunityToDownload(
                photo,
                requireContext(),
                PosterOverlayView(requireContext(), linkImages = mutableListOf(photo)) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                            downloadImage(url, requireContext())
                        } else {
                            when {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                    downloadImage(url, requireContext())
                                }
                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                    showRequestPermissionRational()
                                }
                                else -> {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                        }
                    }
                }
            )
        }
    }


    private fun setAdapter() {
        listAdapter = ResultChallengeAdapter()
        binding.listOfResults.adapter = listAdapter

        listAdapter?.onImageClicked = { clickedView, photo ->
            (clickedView as ShapeableImageView).showImageWithOpportunityToDownload(
                photo,
                requireContext(),
                PosterOverlayView(requireContext(), linkImages = mutableListOf(photo)) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                            downloadImage(url, requireContext())
                        } else {
                            when {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                    downloadImage(url, requireContext())
                                }
                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                    showRequestPermissionRational()
                                }
                                else -> {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun loadMyResult(challengeId: Int) {
        viewModel.loadChallengeResult(challengeId)
    }

    private fun listeners() {
        viewModel.myResultError.observe(viewLifecycleOwner) {
            binding.noData.visibility = View.VISIBLE
        }

        viewModel.myResult.observe(viewLifecycleOwner) {
            (binding.listOfResults.adapter as ResultChallengeAdapter).submitList(it?.reversed())
        }

        viewModel.createReport.observe(viewLifecycleOwner){ createReportBoolean ->
            if(createReportBoolean){
                unblockBtn()
                clearDraft()
                idChallenge?.let {  viewModel.loadChallengeResult(it) }

            }
        }

        viewModel.createReportError.observe(viewLifecycleOwner){
            unblockBtn()
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    // TODO Вынести работу с draft в отдельный репозиторий
    private fun clearDraft(){
        idChallenge?.let {
            requireContext().getSharedPreferences("report${it}", 0).edit().clear().apply()
            checkExistingDraft(it)
        }

    }

    private fun blockBtn(){
        binding.publicDraftBtn.isEnabled = false
        binding.editDraftBtn.isEnabled = false
    }

    private fun unblockBtn(){
        binding.publicDraftBtn.isEnabled = true
        binding.editDraftBtn.isEnabled = true
    }



    companion object {

        @JvmStatic
        fun newInstance(challengeId: Int) =
            MyResultChallengeFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHALLENGER_ID, challengeId)
                }
            }
    }
}