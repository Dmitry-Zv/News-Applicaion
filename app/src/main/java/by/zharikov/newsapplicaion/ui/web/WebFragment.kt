package by.zharikov.newsapplicaion.ui.web

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.databinding.FragmentWebBinding


class WebFragment : Fragment() {

    private var _binding: FragmentWebBinding? = null
    private val mBinding get() = _binding!!
    private val bundleArgs: WebFragmentArgs by navArgs()
    private lateinit var article: Article

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mBinding.webView.canGoBack()) mBinding.webView.goBack()
                    else {
                        val bundle = bundleOf("article" to article)
                        arguments?.getInt("arg_int")?.let { bundle.putInt("argInt", it) }
                        view?.findNavController()
                            ?.navigate(R.id.action_webFragment_to_detailFragment, bundle)
                    }
                }

            }
        )
    }

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
        mBinding.webView.loadUrl(article.url.toString())
    }


}