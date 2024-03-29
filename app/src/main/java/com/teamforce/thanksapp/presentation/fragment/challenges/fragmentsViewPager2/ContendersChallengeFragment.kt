package com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentContendersChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.challenge.ContendersAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerLastItemForComments
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_CREATOR_ID
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_ID
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_REPORT_ID
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_TYPE
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGE_ACTIVE
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.SHOW_CONTENDERS
import com.teamforce.thanksapp.presentation.fragment.challenges.RequestRejectionDialogFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SettingsChallengeFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CommentsViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ContendersChallengeViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ResultsChallengeViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.showImageWithOpportunityToDownload
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContendersChallengeFragment : Fragment(R.layout.fragment_contenders_challenge) {

    private val binding: FragmentContendersChallengeBinding by viewBinding()

    private val viewModel: ContendersChallengeViewModel by activityViewModels()
    private val resultChallengeViewModel: ResultsChallengeViewModel by activityViewModels()

    private var idChallenge: Int? = null
    private var creatorId: Int? = null
    private var typeOfChallenge: ChallengeType? = null
    private var showContenders: Boolean? = null
    private var challengeActive: Boolean? = null

    private var contendersAdapter: ContendersAdapter? = null

    private var currentReportId: Int? = null





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
            typeOfChallenge = it.serializable(CHALLENGER_TYPE)
            creatorId = it.getInt(CHALLENGER_CREATOR_ID)
            showContenders = it.getBoolean(SHOW_CONTENDERS)
            challengeActive = it.getBoolean(CHALLENGE_ACTIVE)
        }
    }

    override fun onDestroyView() {
        binding.contendersRv.adapter = null
        contendersAdapter = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (typeOfChallenge != null && creatorId != null && showContenders != null && challengeActive != null){
            contendersAdapter = ContendersAdapter(applyClickListener = ::apply,
                refuseClickListener = ::refuse,
                onLikeClicked = ::likeReport, onCommentClicked = ::onCommentClicked,
                typeOfChallenge = typeOfChallenge!!,
                onContendersClicked = ::onContendersClicked,
                showContenders = showContenders!!,
                challengeActive = challengeActive!!,
                onLongLikeClicked = ::onLikeLongClicked
            )
        }
        binding.contendersRv.adapter = contendersAdapter
        binding.contendersRv.addItemDecoration(
            VerticalDividerLastItemForComments()
        )

        contendersAdapter?.onImageClicked = { clickedView, photo ->
            (clickedView as ShapeableImageView).showImageWithOpportunityToDownload(
                photo,
                requireContext(),
                PosterOverlayView(requireContext(), linkImages = mutableListOf(photo)) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

        loadParticipants()
        setData()
        listeners()

        viewModel.internetError.observe(viewLifecycleOwner){
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        resultChallengeViewModel.needUpdateComment.observe(viewLifecycleOwner){
            if(it){
                updateCommentAmount(resultChallengeViewModel.commentPositionForUpdate())
                resultChallengeViewModel.setNeedUpdateCommentState(false)
            }
        }

    }

    private fun updateCommentAmount(position: Int){
        loadParticipants()
        contendersAdapter?.notifyItemChanged(position)
    }


    private fun listeners() {

        viewModel.contendersError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
        viewModel.checkReportError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
        viewModel.likeError.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadParticipants() {
        idChallenge?.let { viewModel.loadContenders(it, currentReportId) }
        setData()
    }

    private fun setData() {
        viewModel.contenders.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.noData.invisible()
                contendersAdapter?.submitList(it)
            }
            if(it?.isEmpty() == true) {
                binding.noData.visible()
            }
        }
    }

    private fun createDialog(
        reportId: Int,
        state: Char
    ) {
        if (idChallenge != null){
            val bundle = Bundle().apply {
                putInt(RequestRejectionDialogFragment.REJECTION_DIALOG_REPORT_ID, reportId)
                putChar(RequestRejectionDialogFragment.REJECTION_DIALOG_STATE, state)
                putInt(RequestRejectionDialogFragment.REJECTION_DIALOG_CHALLENGE_ID, idChallenge!!)
            }
            binding.root.findNavController().navigateSafely(
                R.id.action_global_requestRejectionDialogFragment,
                bundle
            )
        }else{
            Log.e(TAG, "ChallengeId is ${idChallenge}")
            Toast.makeText(requireContext(), getString(R.string.smthWentWrong), Toast.LENGTH_SHORT).show()
        }
    }


    private fun onCommentClicked(reportId: Int, position: Int){

        resultChallengeViewModel.setCommentPositionForUpdate(position)
        val bundle = Bundle().apply {
            putInt(Consts.OBJECTS_COMMENT_ID, reportId)
            putParcelable(Consts.OBJECTS_COMMENT_TYPE, ObjectsComment.REPORT_OF_CHALLENGE)
        }
        view?.findNavController()?.navigateSafely(R.id.action_global_commentsBottomSheetFragment, bundle)

    }

    private fun onLikeLongClicked(reportId: Int) {
        val bundle = Bundle().apply {
            putInt(Consts.LIKE_TO_OBJECT_ID, reportId)
            putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.REPORT_OF_CHALLENGE)
        }
        view?.findNavController()?.navigateSafely(R.id.action_global_reactionsFragment, bundle)

    }


    companion object {
        const val TAG = "ContendersChallengeFragment"

        @JvmStatic
        fun newInstance(challengeId: Int, creatorId: Int,
                        typeOfChallenge: ChallengeType, showContenders: Boolean, challengeActive: Boolean) =
            ContendersChallengeFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHALLENGER_ID, challengeId)
                    putSerializable(CHALLENGER_TYPE, typeOfChallenge)
                    putInt(CHALLENGER_CREATOR_ID, creatorId)
                    putBoolean(SHOW_CONTENDERS, showContenders)
                    putBoolean(CHALLENGE_ACTIVE, challengeActive)
                }
            }
    }

    private fun apply(reportId: Int, state: Char) {
        // currentPositionItem = position
        idChallenge?.let { viewModel.checkReport(reportId, state, " ", it) }
    }

    private fun refuse(reportId: Int, state: Char) {
        // Log.d(TAG, "onViewCreated: refused item index $position")
        // currentPositionItem = position
        createDialog(reportId, state)
    }

    private fun likeReport(reportId: Int, positionElement: Int) {
        viewModel.pressLike(reportId, positionElement)

        viewModel.likeResult.observe(viewLifecycleOwner){
            it?.let { likeResponse ->
                contendersAdapter?.updateLikesCount(likeResponse.position, likeResponse.likesAmount, likeResponse.isLiked)
            }
        }
    }

    private fun onContendersClicked(reportId: Int){
    }


}