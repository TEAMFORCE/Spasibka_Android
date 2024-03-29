package com.teamforce.thanksapp.presentation.activity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ActivityMainBinding
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.presentation.fragment.auth.LoginFragment.Companion.ARG_REF
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.Locals.ContextUtils
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IMainAction {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    val viewModel: MainViewModel by viewModels()
    val notificationsSharedViewModel: NotificationSharedViewModel by viewModels()
    var sharedKey: String? = null
    var isDialogShown = false
    lateinit var deviceId: String


    private val navController by lazy {
        val navFragment =
            supportFragmentManager.findFragmentById(R.id.navContainer) as NavHostFragment
        navFragment.findNavController()
    }

    override fun attachBaseContext(newBase: Context) {
        // Получение текущего языка
        val preferences = newBase.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)
        val currentLanguage = preferences.getString(Consts.CURRENT_LANGUAGE, Lang.SYSTEM.lang)
        if (currentLanguage != Lang.SYSTEM.lang) {
            val localeToSwitchTo = Locale(currentLanguage!!)
            val localeUpdatedContext: ContextWrapper =
                ContextUtils.updateLocale(newBase, localeToSwitchTo)
            super.attachBaseContext(localeUpdatedContext)
        } else {
            val systemLang =
                ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
            Log.e("Activity", "${systemLang}")
            val localeUpdatedContext = systemLang?.let {
                ContextUtils.updateLocale(newBase, Locale(it.language), true)
            }

            super.attachBaseContext(localeUpdatedContext)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeeplink(intent)
    }

    private fun handleDeeplink(intent: Intent?) {
        sharedKey = intent?.data?.getQueryParameter(ARG_REF)
        sharedKey?.let { viewModel.getOrgByInvitation(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        viewModel.loadLocalBrandTheme()
        setStatusBarColor()
        deviceId = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        handleDeeplink(intent)
        setStartDestinationAccordingToAuthorize(navGraph)

        viewModel.orgByInvitationError.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            if (viewModel.isUserAuthorized() && viewModel.isCurrentOrgNotNull()) {
                viewModel.loadUserProfile()
                setStartDestination(R.id.mainFlowFragment, navGraph)
            } else {
                setStartDestination(R.id.signFlowFragment, navGraph)
            }
        }
        viewModel.profileError.observe(this) {
            if (it == Consts.UNAUTHORIZED) {
                viewModel.logout(deviceId)
                setStartDestination(R.id.signFlowFragment, navGraph)
            }
        }

    }

    private fun setStatusBarColor() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        val gradientDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_for_status_bar,
            null
        ) as GradientDrawable
        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }

        window.setBackgroundDrawable(gradientDrawable)
    }

    /*
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
     */

    private fun setStatusBarGradient() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val startColor = Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor)
        val endColor = Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor)

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        )

        window.statusBarColor = Color.TRANSPARENT
        window.setBackgroundDrawable(gradientDrawable)

    }


    private fun setStartDestinationAccordingToAuthorize(navGraph: NavGraph) {
        if (sharedKey != null) {
            if (viewModel.isUserAuthorized()) {
                viewModel.loadUserProfile()
                viewModel.profile.observe(this) {
                    checkCurrentUserOrg(navGraph, it)
                }
            } else {
                setStartDestination(R.id.signFlowFragment, navGraph)
            }
        } else {
            // Старая авторизация без sharedKey
            if (viewModel.isUserAuthorized() && viewModel.isCurrentOrgNotNull()) {
                viewModel.loadUserProfile()
                setStartDestination(R.id.mainFlowFragment, navGraph)
            } else {
                setStartDestination(R.id.signFlowFragment, navGraph)
            }
        }

    }

    private fun checkCurrentUserOrg(navGraph: NavGraph, profile: ProfileModel) {
        viewModel.orgByInvitation.observeOnce(this) {
            if (it != null) {
                if (profile.profile.organizationId == it.organizationId) {
                    setStartDestination(R.id.mainFlowFragment, navGraph)
                } else {
                    if (!isDialogShown) {
                        isDialogShown = true
                        callChangeOrgDialog(navGraph, it.organizationName)
                    }
                }
            }

        }
    }

    private fun setStartDestination(startDestId: Int, navGraph: NavGraph) {
        navGraph.setStartDestination(startDestId)
        navController.graph = navGraph
    }

    private fun callChangeOrgDialog(navGraph: NavGraph, orgName: String) {


        val builder = AlertDialog.Builder(this)
        builder.setTitle(this.getString(R.string.main_activity_invite_to_org, orgName))

        builder.setPositiveButton(this.getString(R.string.yes)) { _, _ ->
            viewModel.logout(deviceId)
            setStartDestination(R.id.signFlowFragment, navGraph)
        }

        builder.setNegativeButton(this.getString(R.string.no)) { _, _ ->
            setStartDestination(R.id.mainFlowFragment, navGraph)
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isUserAuthorized()) {
            notificationsSharedViewModel.checkNotifications()
        }
    }


    override fun showSuccessSendingCoins(count: Int, message: String, name: String) {
        TODO("Not yet implemented")
    }

    companion object {
        const val TAG = "MainActivity"
    }


}
