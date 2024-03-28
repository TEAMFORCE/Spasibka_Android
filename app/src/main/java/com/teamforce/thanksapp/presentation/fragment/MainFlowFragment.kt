package com.teamforce.thanksapp.presentation.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentMainFlowBinding
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.presentation.activity.MainActivity
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.DetailsMainChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.profileScreen.ProfileFragment
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Consts.PROFILE_DATA
import com.teamforce.thanksapp.utils.Locals.ContextUtils
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainFlowFragment : BaseFlowFragment(
    R.layout.fragment_main_flow, R.id.nav_host_fragment_main
){

    private var _binding: FragmentMainFlowBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    private val viewModel: MainFlowViewModel by viewModels()

    private var sourceIsLoginFragment: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainFlowBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sourceIsLoginFragment = it.getBoolean(LOGIN_FRAGMENT_SOURCE, false)
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStackImmediate()
            } else {
                showExitConfirmationDialog()
            }
        }
    }

    private fun showExitConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setMessage(requireContext().getString(R.string.main_flow_close_dialog_text))
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(requireContext().getString(R.string.main_flow_close_dialog_yes)) { dialog, _ ->
            requireActivity().finish()
            dialog.dismiss()
        }
        alertDialog.setNegativeButton(requireContext().getString(R.string.main_flow_close_dialog_no)) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    private fun setBlurForBottomAppBar() {
        val radius = 5f
        val rootView = binding.coordinatorLayout
        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            binding.blurView.setupWith(
                rootView,
                RenderScriptBlur(requireContext())
            ) // or RenderEffectBlur
                .setBlurRadius(radius)
        } else {
            binding.blurView.setupWith(rootView, RenderEffectBlur()) // or RenderEffectBlur
                .setBlurRadius(radius)
        }

    }


    override fun setupNavigation(navController: NavController) {
        setBlurForBottomAppBar()
        binding.bottomNavigation.setupWithNavController(navController)
        binding.fab.setOnClickListener {
            navController.navigate(
                R.id.transaction_graph,
                null
            )
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.mainScreenFragment &&
                destination.id != R.id.balanceFragment &&
                destination.id != R.id.challengeContainerFragment &&
                destination.id != R.id.benefitFragment) {
                hideBottomNavigation()
            } else {
                showBottomNavigation()
            }
        }

        setupBottomBarTheme()
    }

    private fun scrollToFirstElement(@IdRes recyclerView: Int) {
        view?.findViewById<RecyclerView>(recyclerView)?.smoothScrollToPosition(0)
    }

    override fun handleIntents(navController: NavController) {
        val deepLink = requireActivity().intent?.data
        val type: String? = deepLink?.pathSegments?.getOrNull(0)
        val id: Int? = deepLink?.pathSegments?.getOrNull(1)?.toIntOrNull()
        Log.e("MainFlowFragment", "Type $type ID ${id}")
        type?.let {
            when (DeepLinkType.valueOfMy(it)) {
                DeepLinkType.CHALLENGE -> {
                    if (id != null) {
                        val initSection = viewModel.getInitChallengeSection(deepLink)
                        Log.e("MainFlowFragment", "InitSection ${initSection}")

                        val bundle = Bundle().apply {
                            putInt(ChallengesConsts.CHALLENGER_ID, id)
                            putParcelable(DetailsMainChallengeFragment.INIT_SECTION_PAGE, initSection)
                        }
                        navController.navigate(
                            R.id.action_global_detailsMainChallengeFragment,
                            bundle,
                            OptionsTransaction().optionForAdditionalInfoFeedFragment
                        )
                        requireActivity().intent.data = null
                    }
                }
                DeepLinkType.OTHER_PROFILE -> {
                    if (id != null) {
                        val bundle = Bundle().apply { putInt(Consts.USER_ID, id) }
                        navController.navigate(
                            R.id.action_global_someonesProfileFragment,
                            bundle,
                            OptionsTransaction().optionForAdditionalInfoFeedFragment
                        )
                        requireActivity().intent.data = null
                    }
                }
                // TODO для перехода в детали бенефита нужен еще marketId
                DeepLinkType.MARKET -> {
                    navController.navigate(
                        R.id.benefitFragment,
                        null,
                        OptionsTransaction().optionForAdditionalInfoFeedFragment
                    )
                    requireActivity().intent.data = null
                    binding.bottomNavigation.menu.getItem(4).isChecked = true
                }
                null -> {
                    navController.navigate(
                        R.id.notificationsFragment,
                        null,
                        OptionsTransaction().optionForAdditionalInfoFeedFragment
                    )
                    requireActivity().intent.data = null
                }
            }
        }

    }

    override fun onViewCreated(navController: NavController) {
        lifecycleScope.launchWhenStarted {
            viewModel.showDialogAboutProfileFields.collectLatest {
                showDialogAboutProfile(navController)
            }
        }
        if (requireActivity().intent.data?.pathSegments?.getOrNull(0) == null) {
            checkingProfileFields(navController)
        }
    }

    private fun checkingProfileFields(navController: NavController) {
        viewModel.loadProfileSettings()
        viewModel.profileSettings.observe(viewLifecycleOwner) {
            updateLocale(it.language)
        }
        viewModel.loadUserProfile()
        viewModel.profile.observe(viewLifecycleOwner) {
            getBrandTheme(it.profile.organizationId!!)
            showSuggestionToLaunchPeriod(navController)
        }
    }

    private fun updateLocale(langFromBack: Lang) {
        val currentLang = ContextUtils.getCurrentLangInAppLangFormat(requireContext())
        if (currentLang != langFromBack) {
            ContextUtils.saveNewLocaleToSP(requireContext(), langFromBack)
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().recreate()
        }
    }

    private fun showDialogAboutProfile(navController: NavController) {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.edit_profile_go_to_settings),
            negativeTextBtn = requireContext().getString(R.string.edit_profile_close),
            title = requireContext().getString(R.string.edit_profile_fill_in_the_profile_data)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)

        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                val bundle = Bundle().apply {
                    putBoolean(ProfileFragment.GO_TO_EDIT_PROFILE, true)
                    putParcelable(PROFILE_DATA, viewModel.profile.value)
                }
                navController.navigate(R.id.profileGraph, bundle)
            }
        })

        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }

    private fun hideBottomNavigation() {
        binding.blurView.clearAnimation()
        binding.fab.hide()
        binding.blurView.animate().translationY(binding.blurView.height.toFloat() + 50f).setDuration(300)
    }

    private fun showBottomNavigation() {
        binding.blurView.clearAnimation()
        binding.blurView.animate().translationY(0f).setDuration(300).withEndAction {
            binding.fab.show()
        }
    }

    private fun setupBottomBarTheme() {

        val fabLayerDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.gradient_drawable_with_image,
            null
        ) as LayerDrawable

        (fabLayerDrawable.findDrawableByLayerId(R.id.fab_background) as GradientDrawable).apply {
            colors = intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.secondaryBrandColor)
            )
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TL_BR
        }

        binding.fab.foreground = fabLayerDrawable

        val layerDrawable =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bottom_bar_item_background,
                null
            ) as LayerDrawable

        (layerDrawable.findDrawableByLayerId(R.id.item_background) as GradientDrawable).setColor(
            Color.parseColor(
                appTheme.secondaryBrandColor
            )
        )

        val itemColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.generalContrastColor)
            )
        )
        binding.bottomNavigation.itemIconTintList = itemColorStateList
        binding.bottomNavigation.itemTextColor = itemColorStateList
    }

    override fun getBrandTheme(currentOrgId: Int) {
        viewModel.loadRemoteBrandingTheme(currentOrgId)
        viewModel.brandingTheme.distinctUntilChanged().observe(viewLifecycleOwner) {
            if (it?.isNewColor == true) {
                refreshApplication()
            }
        }
    }

    private fun refreshApplication() {
        requireActivity().apply {
            finish()
            overridePendingTransition(0, 0)
            startActivity(requireActivity().intent)
            overridePendingTransition(0, 0)
        }

    }

    override fun showSuggestionToLaunchPeriod(navController: NavController) {
        checkingCurrentPeriodExisted(navController)
    }

    private fun checkingCurrentPeriodExisted(navController: NavController) {
        viewModel.loadCurrentPeriod()
        viewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            if (currentPeriod == null && viewModel.userIsHeadOfDepartment() && !viewModel.profile.value?.profile?.organizationId.toString()
                    .isNullOrEmptyMy()
            ) {
                showSnackBarLaunchPeriod(navController)
            }
        }
    }

    private fun showSnackBarLaunchPeriod(navController: NavController) {
        val snackBar = Snackbar.make(
            binding.coordinatorLayout,
            getString(R.string.main_activity_create_period),
            Snackbar.LENGTH_LONG
        )
//        val view = snackBar.view
//        val params = view.layoutParams as FrameLayout.LayoutParams
//        params.gravity = Gravity.TOP
//        view.layoutParams = params
        snackBar.setAction(R.string.yes) {
            val bundle = Bundle()
            bundle.putBoolean(Consts.RETURN_AFTER_FINISHIED, true)
            navController.navigateSafely(R.id.action_global_settingsPeriodFragment2, bundle)
        }
        snackBar.show()


    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

    companion object {
        const val LOGIN_FRAGMENT_SOURCE = "login_fragment_source"
    }


    enum class DeepLinkType {
        OTHER_PROFILE, CHALLENGE, MARKET;

        companion object {
            fun valueOfMy(value: String?) =
                DeepLinkType.values().firstOrNull { it.name == value?.uppercase() }
        }
    }
}

