package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.model.layer.NullLayer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.thanksapp.BuildConfig
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.profile.ProfileStatsEntity
import com.teamforce.thanksapp.databinding.FragmentProfileBinding
import com.teamforce.thanksapp.domain.models.profile.FormatOfWork
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.Status
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.viewmodel.profile.ProfileViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Consts.PROFILE_DATA
import com.teamforce.thanksapp.utils.Consts.UNAUTHORIZED
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
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate),
    OnMapReadyCallback {

    private val viewModel: ProfileViewModel by viewModels()

    private var profile: ProfileModel? = null


    var adapter: ArrayAdapter<String>? = null
    private var goToEditProfile: Boolean = false


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showDialogAboutPermissions()
            }
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
            goToEditProfile = it.getBoolean(GO_TO_EDIT_PROFILE)
            profile = it.parceleable(PROFILE_DATA)
        }
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        binding.map.onCreate(mapViewBundle)
        binding.map.getMapAsync(this)
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
                        val uri = Uri.fromParts("package", "${BuildConfig.APPLICATION_ID}", null)
                        data = uri
                    }
                startActivity(reqIntent)

            }
            .show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (goToEditProfile) {
            transactionToEditFragment()
            goToEditProfile = false
        }
        initViews()
        requestData()
        setData()
        btnListeners()
        dataObservers(view)
        swipeToRefresh()
        handleTopAppBar()

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



    private fun dataObservers(view: View) {
        // TODO Странно работает обновление, оно идет, и все ок, но идет 1 запрос почему то
        // Как и должно быть, но по идее должно быть много лишних запросов, но их нет, разобраться
        viewModel.isSuccessfulOperation.observe(viewLifecycleOwner) {
            if (it) {
                requestData()
                // setData(adapter)
            }
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
            showConnectionError()
        }

        binding.apply {
            viewModel.getStats()
            viewModel.tags.observe(viewLifecycleOwner) {
                if (it is Result.Success && it.value.isNotEmpty()) {
                    setStats(it.value)
                } else {
                    binding.statsCard.invisible()
                }
            }

            viewModel.profileError.observe(viewLifecycleOwner) {
                val deviceId = Settings.Secure.getString(
                    requireContext().applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                if (it == UNAUTHORIZED) {
                    viewModel.logout(deviceId)
                    activityNavController().navigateSafely(R.id.action_global_signFlowFragment)
                }
            }
        }
    }

    private fun btnListeners() {
        binding.editBtn.setOnClickListener {
            transactionToEditFragment()
        }

        binding.settingsBtn.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_settingsFragment2, null,
                OptionsTransaction().optionForEditProfile
            )
        }
        binding.statusBar.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(PROFILE_DATA, profile)
            findNavController().navigateSafely(
                R.id.action_profileFragment_to_chooseStatusDialogFragment,
                bundle
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

        binding.mobileLinear.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${binding.mobileValueTv.text}")
            startActivity(intent)
        }

        binding.emailLinear.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${binding.emailValueTv.text}")
            startActivity(intent)
        }
    }


    private fun setStats(data: List<ProfileStatsEntity>) {
        binding.statsList.setStats(data)
    }

    private fun showConnectionError() {
        // binding.error.root.visible()
        binding.allContent.invisible()
        binding.scroll.invisible()
    }

    private fun hideConnectionError() {
        // binding.error.root.invisible()
        binding.allContent.visible()
        binding.scroll.visible()

    }

    private fun requestData() {
        viewModel.loadUserProfile()
        viewModel.getStats()
        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.visible()
        binding.scroll.invisible()
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        viewModel.profile.observe(viewLifecycleOwner) {
            binding.shimmerLayout.stopShimmer()
            binding.scroll.visible()
            binding.shimmerLayout.invisible()
            hideConnectionError()
            profile = it
            binding.leadNameTv.text = "${it.profile.firstname} ${it.profile.surname}"
            binding.firstNameValueTv.text = it.profile.firstname
            binding.surnameValueTv.text = it.profile.surname
            binding.middleNameValueTv.text = it.profile.middleName
            binding.userAvatar.indicatorText = it?.rate.toString() + "%"
            if (it.profile.awards.isNotEmpty()) {
                binding.awardIv.visible()
                binding.awardIv.setImageAward(it.profile.awards.first().cover)
            }else{
                binding.awardIv.invisible()
            }
            if(it.profile.location?.city != null) binding.locationTv.text = it.profile.location?.city
            else binding.locationTv.invisible()
            setJobPositionCard(it)
            setStatus(it)
            setUserContacts(it)
            setUserAvatar(it)
            setBirthday(it)
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

    private fun setUserAvatar(profile: ProfileModel) {
        binding.userAvatar.setAvatarImageOrInitials(profile.profile.photo,"${profile.profile.firstname} ${profile.profile.surname}")

        binding.userAvatar.setOnClickListener { view ->
            profile.profile.photo?.let { photo ->
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

    private fun setUserContacts(profile: ProfileModel) {
        profile.profile.contacts.forEach {
            if (it.contactType == "@") {
                binding.emailLabelTv.visible()
                binding.emailLinear.visible()
                binding.emailValueTv.text = it.contactId
            } else {
                binding.mobileLabelTv.visible()
                binding.mobileLinear.visible()
                binding.mobileValueTv.text = it.contactId
            }
        }
    }

    private fun setJobPositionCard(profile: ProfileModel) {
        if (!profile.profile.roles.isNullOrEmpty()) {
            binding.roleCard.visible()
            binding.roleValueTv.text = profile.profile.roles
        }else{
            binding.roleCard.invisible()
        }
        binding.companyValueTv.text = profile.profile.organization
        if (!profile.profile.jobTitle.isNullOrEmpty()) {
            binding.positionLinear.visible()
            binding.positionValueTv.text = profile.profile.jobTitle
        } else {
            binding.positionLinear.invisible()
        }

        if (profile.profile.department.isNotNullOrEmptyMy()) {
            binding.departmentCompany.visible()
            binding.departmentValueTv.text = profile.profile.department
        } else {
            binding.departmentCompany.invisible()
        }
    }

    private fun setStatus(profile: ProfileModel) {
        val statusText = when (profile.profile.status) {
            Status.VACATION.value -> requireContext().getString(R.string.edit_profile_vacation)
            Status.SICK_LEAVE.value -> requireContext().getString(R.string.edit_profile_sickLeave)
            else -> requireContext().getString(R.string.edit_profile_work)
        }
        val formatOfWork = when (profile.profile.formatOfWork) {
            FormatOfWork.OFFICE.value -> requireContext().getString(R.string.edit_profile_office)
            FormatOfWork.REMOTE.value -> requireContext().getString(R.string.edit_profile_remote)
            else -> requireContext().getString(R.string.edit_profile_remote)
        }
        binding.status.text = statusText
        binding.formatWorkTv.text = formatOfWork
    }

    private fun swipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            requestData()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun transactionToEditFragment() {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_DATA, profile)

        findNavController().navigateSafely(
            R.id.action_global_editProfileFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
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


    private fun initViews() {
        viewModel.isLoading.observe(
            viewLifecycleOwner
        ) { isLoading ->
            if (isLoading) {
                binding.allContent.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = true
            } else {
                binding.allContent.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false

            }
        }
    }

    companion object {

        const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        const val TAG = "ProfileFragment"
        const val GO_TO_EDIT_PROFILE = "go_to_edit_profile"
    }

    override fun applyTheme() {

        binding.statusBar.setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))

    }

    override fun onMapReady(p0: GoogleMap) {
        p0.uiSettings.isScrollGesturesEnabled = false
        viewModel.location.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.city.isNotBlank()) {
                    binding.locationTv.text = it.city
                }else{
                    binding.locationTv.invisible()
                }

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.lat, it.lon))
                    .zoom(12f) // Указываем уровень масштабирования
                    .build()

                p0.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }else{
                binding.locationCard.invisible()
            }
        }
    }
}
