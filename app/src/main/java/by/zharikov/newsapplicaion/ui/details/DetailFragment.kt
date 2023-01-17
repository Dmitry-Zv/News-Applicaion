package by.zharikov.newsapplicaion.ui.details

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.FragmentDetailBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.ArticlePreferencesRepository
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModel
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesUseCase
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModelFactory
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.ToolBarSetting
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import by.zharikov.newsapplicaion.worker.UploadWorker
import com.bumptech.glide.Glide


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val mBinding get() = _binding!!
    private val bundleArgs: DetailFragmentArgs by navArgs()
    private val prefCounter: SharedPreferences by lazy {
        requireContext().getSharedPreferences("COUNTER_SHARED_PREF", Context.MODE_PRIVATE)
    }
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private lateinit var viewModel: ArticlePreferencesViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var toolBarSetting: ToolBarSetting


    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSetting.setUpToolBar("Detail", Constants.FRAGMENT_DETAILED)
        var counter = prefCounter.getInt("Counter", 0)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        val articlePreferencesRepository = ArticlePreferencesRepository(pref)
        val articlePreferencesUseCase =
            ArticlePreferencesUseCase(articlePreferencesRepository, articleEntityRepository)
        viewModel = ViewModelProvider(
            this,
            ArticlePreferencesViewModelFactory(articlePreferencesUseCase)
        )[ArticlePreferencesViewModel::class.java]

        val articleArg = bundleArgs.article
        articleArg.let { article ->

            if (article.urlToImage == null) mBinding.headerImage.setImageResource(R.drawable.news)
            else article.urlToImage.let {
                Glide.with(this).load(article.urlToImage).into(mBinding.headerImage)
            }
            mBinding.headerImage.clipToOutline = true
            mBinding.articleDetailTitleText.text = article.title
            mBinding.articleDetailDescriptionText.text = article.description
            mBinding.articleDetailButton.setOnClickListener {
                val bundle = bundleOf("article_arg" to article)
                arguments?.getInt("argInt")?.let { it1 -> bundle.putInt("arg_int", it1) }
                view.findNavController().navigate(R.id.action_detailFragment_to_webFragment, bundle)

            }
        }
        val uiArticle = UiArticle(articleArg, pref.getBoolean(articleArg.url, false))
        if (uiArticle.isLiked) mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_24)
        else mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
        mBinding.iconFavourite.setOnClickListener {
            uiArticle.isLiked = !uiArticle.isLiked
            Log.d("idTitle", articleArg.title.toString())
            if (uiArticle.isLiked) {
                viewModel.addArticle(articleArg)
                mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_24)
                Toast.makeText(requireContext(), "SAVED", Toast.LENGTH_SHORT).show()
                counter++
                prefCounter.edit().putInt("Counter", counter).apply()

            } else {
                viewModel.deleteEntityArticle(articleArg)
                mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
                Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
                collectLatestLifecycleFlow(sharedViewModel.articles) { articles ->
                    if (!articles.contains(uiArticle.article)) {
                        if (counter > 0) counter--
                    }
                }
            }
            setOnTimeWorkRequest()

            sharedViewModel.setCounter(counter)
            prefCounter.edit().putInt("Counter", counter).apply()
        }



        mBinding.iconShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, articleArg.url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        mBinding.iconBack.setOnClickListener {
            when (arguments?.getInt("argInt")) {
                1 -> view.findNavController().navigate(R.id.action_detailFragment_to_mainFragment)
                2 -> view.findNavController().navigate(R.id.action_detailFragment_to_searchFragment)
                3 -> view.findNavController()
                    .navigate(R.id.action_detailFragment_to_favouriteFragment)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun setOnTimeWorkRequest() {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .build()
        WorkManager.getInstance(requireContext())
            .enqueue(uploadRequest)
    }
}