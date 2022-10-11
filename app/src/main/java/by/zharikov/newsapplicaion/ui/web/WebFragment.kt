package by.zharikov.newsapplicaion.ui.web

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.databinding.FragmentWebBinding


class WebFragment : Fragment() {

    private var _binding: FragmentWebBinding? = null
    private val mBinding get() = _binding!!
    private val bundleArgs: WebFragmentArgs by navArgs()
    private lateinit var article: Article

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.webView.webViewClient = WebViewClient()
        val webSetting = mBinding.webView.settings
        webSetting.javaScriptEnabled = true
        article = bundleArgs.articleArg
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSetting, WebSettingsCompat.FORCE_DARK_ON)
            }
        }
        mBinding.webView.loadUrl(article.url.toString())
    }


}