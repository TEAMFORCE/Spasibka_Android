package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.databinding.FragmentSomeonesProfileBinding
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.NegativeButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.SomeonesProfileViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.showImageWithOpportunityToDownload
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImageAward
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class SomeonesProfileFragment :
    BaseFragment<FragmentSomeonesProfileBinding>(FragmentSomeonesProfileBinding::inflate),
    OnMapReadyCallback {

    private val viewModel: SomeonesProfileViewModel by viewModels()

    private var userId: Int? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val data =  super.onCreateView(inflater, container, savedInstanceState)
        initGoogleMap(savedInstanceState)
        return data
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(USER_ID)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        requestData()
        setData()
        swipeToRefresh()
        showThanksBtn()

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }
        binding.thanksBtn.setOnClickListener {
            val user = viewModel.getUserBean()
            val bundle = Bundle()
            bundle.putParcelable(Consts.PROFILE_DATA, user)
            if (user != null)
                findNavController().navigate(
                    R.id.transaction_graph,
                    bundle,
                    OptionsTransaction().optionForTransaction
                )
        }

        binding.emailLinear.setOnClickListener {
            copyTextToClipboard(
                binding.emailValueTv.text.toString(),
                getString(R.string.email_label),
                requireContext()
            )
        }

        binding.mobileLinear.setOnClickListener {
            copyTextToClipboard(
                binding.mobileValueTv.text.toString(),
                getString(R.string.mobile_label),
                requireContext()
            )
        }

        // TODO Возможно стоит диалоговое окно сначала показать, мол, позвонить ли или отправить письмо
        binding.mobileLinear.setOnLongClickListener {
            showCallUserDialog()
            return@setOnLongClickListener true
        }

        binding.emailLinear.setOnLongClickListener {
            showEmailUserDialog()
            return@setOnLongClickListener true
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.allContent.invisible()
                binding.swipeRefreshLayout.isRefreshing = true
            } else {
                binding.allContent.visible()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showCallUserDialog() {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.someone_profile_call_dialog_positive_btn),
            negativeTextBtn = requireContext().getString(R.string.someone_profile_dialog_negative_btn),
            title = requireContext().getString(R.string.someone_profile_call_dialog_title),
            subtitle = requireContext().getString(
                R.string.someone_profile_call_dialog_subtitle,
                binding.mobileValueTv.text
            )
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)
        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                dialogFragment.dismiss()
                callToUser()
            }
        })

        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }


    private fun callToUser() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${binding.mobileValueTv.text}")
        startActivity(intent)
    }

    private fun emailToUser() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:${binding.emailValueTv.text}")
        startActivity(intent)
    }

    private fun showEmailUserDialog() {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.someone_profile_email_dialog_positive_btn),
            negativeTextBtn = requireContext().getString(R.string.someone_profile_dialog_negative_btn),
            title = requireContext().getString(R.string.someone_profile_email_dialog_title),
            subtitle = requireContext().getString(
                R.string.someone_profile_email_dialog_subtitle,
                binding.emailValueTv.text
            )
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)
        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                dialogFragment.dismiss()
                emailToUser()
            }
        })

        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }


    override fun applyTheme() {

    }

    private fun showThanksBtn() {
        Log.e(TAG, "SomeonesUserId - ${userId} and MyId - ${viewModel.getProfileId()}")
        if (viewModel.getProfileId() == userId.toString()) binding.thanksBtn.invisible()
    }


    private fun handleTopAppBar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val initialSize = 132.dp
        val finalSize = 64.dp

        val initialTranslationY = 66.dp
        val finalTranslationY = 25.dp

        var currentSize = initialSize
        var currentTranslationY = initialTranslationY

        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val percentage = abs(verticalOffset.toFloat()) / appBarLayout.totalScrollRange
            val newSize = (initialSize - (initialSize - finalSize) * percentage).toInt()
            val newTranslationY =
                (initialTranslationY - (initialTranslationY - finalTranslationY) * percentage).toInt()

            if (newSize != currentSize || newTranslationY != currentTranslationY) {
                currentSize = newSize
                currentTranslationY = newTranslationY

                updateAvatarViewParams(newSize, newTranslationY)
            }
        }
    }

    private fun updateAvatarViewParams(newSize: Int, newTranslationY: Int) {
        val layoutParams = binding.userAvatar.layoutParams
        layoutParams.width = newSize
        layoutParams.height = newSize
        binding.userAvatar.layoutParams = layoutParams
        binding.userAvatar.translationY = newTranslationY.toFloat()

        binding.userAvatar.requestLayout()
    }



    private fun swipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            requestData()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun requestData() {
        userId?.let {
            viewModel.loadAnotherUserProfile(it)
        }
    }

    private fun setData() {
        viewModel.anotherProfile.observe(viewLifecycleOwner) {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.invisible()
            binding.firstNameValueTv.text = it.profile.firstname
            binding.surnameValueTv.text = it.profile.surname
            binding.leadNameTv.text = "${it.profile.firstname} ${it.profile.surname}"
            binding.middleNameValueTv.text = it.profile.middleName
            binding.companyValueTv.text = it.profile.organization
            binding.userAvatar.indicatorText = it?.rate.toString() + "%"

            if (it.profile.awards.isNotEmpty()) {
                binding.awardIv.setImageAward(it.profile.awards.first().cover)
            }else{
                binding.awardIv.invisible()
            }
            setJobPositionCard(it)
            setBirthday(it)


            if (it.profile.jobTitle.isNullOrEmpty()) {
                binding.positionLinear.invisible()
            } else {
                binding.positionLinear.visible()
                binding.positionValueTv.text = it.profile.jobTitle
            }

            if (it.profile.contacts.size == 1) {
                if (it.profile.contacts[0].contactType == "@") {
                    binding.emailLabelTv.visible()
                    binding.emailLinear.visible()
                    binding.emailValueTv.text = it.profile.contacts[0].contactId
                } else {
                    binding.mobileLabelTv.visible()
                    binding.mobileLinear.visible()
                    binding.mobileValueTv.text = it.profile.contacts[0].contactId
                }
            } else if (it.profile.contacts.size == 2) {
                binding.mobileLabelTv.visible()
                binding.mobileLinear.visible()
                binding.emailLabelTv.visible()
                binding.emailLinear.visible()
                if (it.profile.contacts[0].contactType == "@") {
                    binding.emailValueTv.text = it.profile.contacts[0].contactId
                    binding.mobileValueTv.text = it.profile.contacts[1].contactId
                } else {
                    binding.emailValueTv.text = it.profile.contacts[1].contactId
                    binding.mobileValueTv.text = it.profile.contacts[0].contactId
                }
            }

            binding.userAvatar.setAvatarImageOrInitials(it.profile.photo, "${it.profile.firstname} ${it.profile.surname}")

            binding.userAvatar.setOnClickListener { view ->
                it.profile.photo?.let { photo ->
                    (view as AvatarView).showImageWithOpportunityToDownload(
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
                                            val url =
                                                "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
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
        }
    }

    private fun setBirthday(profile: ProfileModel) {
        if (!profile.profile.birthday.isNullOrEmpty() && profile.profile.birthday != "null") {
            binding.birthdayLabelTv.visible()
            binding.birthdayValueTv.visible()
            if (profile.profile.showYearOfBirth) {
                binding.birthdayValueTv.text =
                    "${parseDateOutputOnlyDateWithYear(profile.profile.birthday, requireContext())} ${getString(R.string.profile_postfix_year)}"
            } else {
                binding.birthdayValueTv.text =
                    parseDateOutputOnlyDateWithoutYear(profile.profile.birthday, requireContext())
            }

        }
    }


    private fun setJobPositionCard(profile: ProfileModel) {
        if (profile.profile.roles != null) {
            binding.roleCard.visible()
            binding.roleValueTv.text = profile.profile.roles
        }
        binding.companyValueTv.text = profile.profile.organization
        if (!profile.profile.jobTitle.isNullOrEmpty()) {
            binding.positionLinear.visible()
            binding.positionValueTv.text = profile.profile.jobTitle
        } else {
            binding.positionLinear.invisible()
        }

        if (profile.profile.department != null) {
            binding.departmentCompany.visible()
            binding.departmentValueTv.text = profile.profile.department
        } else {
            binding.departmentCompany.invisible()
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        p0.uiSettings.isScrollGesturesEnabled = false
        viewModel.location.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.locationTv.text = it.city
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.lat, it.lon))
                    .zoom(12f) // Указываем уровень масштабирования
                    .build()

                p0.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }else{
                binding.locationCard.invisible()
                binding.locationTv.invisible()
            }
        }
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(ProfileFragment.MAPVIEW_BUNDLE_KEY)
        }
        binding.map.onCreate(mapViewBundle)
        binding.map.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
//        MapKitFactory.getInstance().onStart()
//        binding.mapView.onStart()
        binding.map.onStart()

    }

    override fun onStop() {
//        binding.mapView.onStop()
//        MapKitFactory.getInstance().onStop()
        binding.map.onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        binding.map.onDestroy()
        super.onDestroyView()
    }

    companion object {
        const val TAG = "SomeonesProfile"
        const val USER_ID = "userId"
    }
}