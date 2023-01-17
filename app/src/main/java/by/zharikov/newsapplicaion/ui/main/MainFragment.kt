package by.zharikov.newsapplicaion.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.adapter.TagAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.TagModelUi
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.data.model.UiState
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.ArticlePreferencesRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.repository.TagRepository
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModel
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesUseCase
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModelFactory
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.ArticleRetrofitUseCase
import by.zharikov.newsapplicaion.utils.*
import by.zharikov.newsapplicaion.worker.UploadWorker

class MainFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener,
    TagClickListener {

    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var tagAdapter: TagAdapter
    private var uiListArticle = mutableListOf<UiArticle>()
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val prefCounter: SharedPreferences by lazy {
        requireContext().getSharedPreferences("COUNTER_SHARED_PREF", Context.MODE_PRIVATE)
    }
    private lateinit var mainViewModel: MainFragmentViewModel
    private var bundle: Bundle? = null
    private var counter: Int = 0
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var tagModelUi: TagModelUi? = null
    private var flag = false
    private lateinit var toolBarSetting: ToolBarSetting
    private lateinit var viewModel: ArticlePreferencesViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (bundle == null) {
            view?.findNavController()?.navigate(R.id.action_mainFragment_to_detailFragment, bundle)
        }
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

            }
        )

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSetting.setUpToolBar("News", Constants.FRAGMENT_MAIN)
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        val articlePreferencesRepository = ArticlePreferencesRepository(pref)
        val articlePreferencesUseCase =
            ArticlePreferencesUseCase(articlePreferencesRepository, articleEntityRepository)
        val articleRetrofitUseCase = ArticleRetrofitUseCase(newsRepository = newsRepository)
        val tagRepository = TagRepository(requireContext())
        viewModel = ViewModelProvider(
            this,
            ArticlePreferencesViewModelFactory(articlePreferencesUseCase)
        )[ArticlePreferencesViewModel::class.java]
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                articleRetrofitUseCase = articleRetrofitUseCase,
                tagRepository = tagRepository
            )
        )[MainFragmentViewModel::class.java]

        collectLatestLifecycleFlow(mainViewModel.tagUiList) { tagUiList ->

            Log.d("TAG_UI_LIST", tagUiList.toString())
            tagAdapter = TagAdapter(tagUiList, this@MainFragment)
            mBinding.recyclerTags.adapter = tagAdapter
            var count = 0
            for (tagUi in tagUiList) {
                if (tagUi.isClicked) {
                    tagModelUi = tagUi
                    flag = true
                } else {
                    count++
                    if (count == 7) flag = false

                }
            }


        }
        counter = prefCounter.getInt("Counter", 0)
        sharedViewModel.setCounter(counter)
        mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
        articleAdapter = ArticleAdapter(uiListArticle, this, this, this)
        mBinding.newsAdapter.adapter = articleAdapter
        mBinding.progressBar.visibility = View.VISIBLE


        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                if (flag) {
                    tagModelUi?.tagModel?.tagName?.let {
                        mainViewModel.getArticlesByCategory(
                            "us",
                            it
                        )
                    }
                } else {
                    mainViewModel.getArticles("ru")
                    Log.d("NEWS_MODEL_TAG", "HELLO")
                }
                observeArticle()
            } else {
                recreateUi()
            }
        }

        mBinding.buttonRetryConnection.setOnClickListener {
            view.findNavController().navigate(R.id.action_mainFragment_self)
        }
        mBinding.refreshLayout.setOnRefreshListener {
            view.findNavController().navigate(R.id.action_mainFragment_self)
        }


    }


    private fun map(articleList: MutableList<Article>): MutableList<UiArticle> {
        val uiList = mutableListOf<UiArticle>()
        for (article in articleList) {
            uiList.add(UiArticle(article, pref.getBoolean(article.url, false)))
        }
        return uiList
    }

    override fun onCellClickListener(article: Article) {
        bundle = bundleOf("article" to article)
        bundle?.putInt("argInt", 1)
        view?.findNavController()?.navigate(R.id.action_mainFragment_to_detailFragment, bundle)
    }

    override fun onFavIconClickListener(uiArticle: UiArticle) {
        Log.d("idTitle", uiArticle.article.title.toString())
        if (uiArticle.isLiked) {
            counter++
            viewModel.addArticle(article = uiArticle.article)
        } else {
            viewModel.deleteEntityArticle(article = uiArticle.article)
            collectLatestLifecycleFlow(sharedViewModel.articles) { articles ->
                Log.d("ARTICLE_TAG", articles.toString())
                Log.d("ARTICLE_TAG", uiArticle.toString())
                if (!articles.contains(uiArticle.article)) {

                    if (counter > 0) counter--

                }
            }


        }
        sharedViewModel.setCounter(counter)
        prefCounter.edit().putInt("Counter", counter).apply()

        setOnTimeWorkRequest()

    }

    override fun onShareIconClickListener(url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onTagClickListener(tagUi: TagModelUi) {
        mBinding.progressBar.visibility = View.VISIBLE
        var count = 0
        mainViewModel.setTagUiList(tagUi)
        collectLatestLifecycleFlow(mainViewModel.tagUiList) { tagModelUiList ->
            tagModelUiList.forEach { tagUi ->
                if (tagUi.isClicked) {
                    tagModelUi = tagUi
                    flag = true
                } else {
                    count++
                    if (count == 7) flag = false
                }
            }
        }
        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                if (flag) {
                    tagModelUi?.tagModel?.tagName?.let {
                        mainViewModel.getArticlesByCategory(
                            "us",
                            it
                        )
                    }
                } else {
                    mainViewModel.getArticles("ru")
                }
                observeArticle()
            } else {
                recreateUi()
            }
        }

    }

    private fun observeArticle() {
        collectLatestLifecycleFlow(mainViewModel.uiState) { uiState ->
            when (uiState) {
                is UiState.Initial -> {
                    mBinding.apply {
                        progressBar.visibility = View.VISIBLE
                    }
                }
                is UiState.ShowArticles -> {
                    mBinding.apply {
                        progressBar.visibility = View.INVISIBLE
                        popularNewsText.visibility = View.VISIBLE
                        newsAdapter.visibility = View.VISIBLE
                        buttonRetryConnection.visibility = View.INVISIBLE
                        connectDescriptionText.visibility = View.INVISIBLE
                        connectText.visibility = View.INVISIBLE
                        imageView.visibility = View.INVISIBLE
                    }
                    if (tagModelUi?.isClicked == true) mBinding.popularNewsText.text =
                        tagModelUi?.tagModel?.tagName
                    else mBinding.popularNewsText.setText(R.string.popular_news)
                    uiListArticle = map(uiState.articles.toMutableList())
                    mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
                    articleAdapter = ArticleAdapter(
                        uiListArticle,
                        this@MainFragment,
                        this@MainFragment,
                        this@MainFragment
                    )
                    mBinding.newsAdapter.adapter = articleAdapter
                }
                is UiState.Error -> Toast.makeText(
                    requireContext(),
                    "Error: ${uiState.exception}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun recreateUi() {
        mBinding.apply {
            progressBar.visibility = View.INVISIBLE
            popularNewsText.visibility = View.INVISIBLE
            newsAdapter.visibility = View.INVISIBLE
            buttonRetryConnection.visibility = View.VISIBLE
            connectDescriptionText.visibility = View.VISIBLE
            connectText.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
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


