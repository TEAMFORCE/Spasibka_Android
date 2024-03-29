package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.BuildConfig
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentPrivacyPolicyBinding


class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private val binding: FragmentPrivacyPolicyBinding by viewBinding()

    private var typeOfDocument: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            typeOfDocument = it.getString(TYPE_OF_DOCUMENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val webView = binding.webView
//        webView.settings.apply {
//            allowFileAccess = true
//            setSupportZoom(true)
//            javaScriptEnabled = true
//        }
      //  webView.webViewClient = WebViewClient()
        val googleCheckPdf = "https://drive.google.com/viewerng/viewer?embedded=true&url="
        val privacyPolicyPdf = "${BuildConfig.URL_PORT}/files/policy_mobile.pdf"
        val userAgreementPdf = "${BuildConfig.URL_PORT}/files/terms_of_use.pdf"
        // https://backdev.teamforce360.com/files/policy_mobile.pdf

//        when(typeOfDocument){
//            WhatKindOfDocument.PRIVACY_POLICY.name -> webView.loadUrl(googleCheckPdf + privacyPolicyPdf)
//            WhatKindOfDocument.USER_AGREEMENT.name -> webView.loadUrl(googleCheckPdf + userAgreementPdf)
//            else -> webView.loadUrl(googleCheckPdf + privacyPolicyPdf)
//        }

        when(typeOfDocument){
            WhatKindOfDocument.PRIVACY_POLICY.name -> {
                binding.pdfView.initWithUrl(
                    url = privacyPolicyPdf,
                    lifecycleCoroutineScope = lifecycleScope,
                    lifecycle = lifecycle
                )
            }
            WhatKindOfDocument.USER_AGREEMENT.name -> {
                binding.pdfView.initWithUrl(
                    url = userAgreementPdf,
                    lifecycleCoroutineScope = lifecycleScope,
                    lifecycle = lifecycle
                )
            }
            else -> {
                // Show message
            }
        }

    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        try {
            downloadManager.enqueue(request)
            Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        const val TYPE_OF_DOCUMENT = "typeOfDocument"


        @JvmStatic
        fun newInstance() =
            PrivacyPolicyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}