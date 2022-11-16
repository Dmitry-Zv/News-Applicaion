package by.zharikov.newsapplicaion.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.adapter.TagAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.data.model.*
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.utils.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener,
    TagClickListener {

    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var articles: List<Article>
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var tagAdapter: TagAdapter
    private var uiListArticle = mutableListOf<UiArticle>()
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val articleToEntityArticle = ArticleToEntityArticle()
    private lateinit var mainViewModel: MainFragmentViewModel
    private var bundle: Bundle? = null
    private var counter: Int = 0
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var tagUiList: List<TagModelUi>
    private var tagModelUi: TagModelUi? = null
    private var flag = false


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

        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        val tagList = TagModels.tagModelList
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                newsRepository = newsRepository,
                articleEntityRepository = articleEntityRepository
            )
        )[MainFragmentViewModel::class.java]
        tagUiList = createListTagModelUi(tagList)
        mainViewModel.setTagUiList(tagUiList)
        mainViewModel.tagUiList.observe(viewLifecycleOwner) { tagUiList ->

            tagAdapter = TagAdapter(tagUiList, this)
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
        counter = pref.getInt("Counter", 0)
        sharedViewModel.setCounter(counter)
        mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
        articleAdapter = ArticleAdapter(uiListArticle, this, this, this)
        mBinding.newsAdapter.adapter = articleAdapter
        mBinding.progressBar.visibility = View.VISIBLE


        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                if (flag) {
                    mBinding.popularNewsText.text = tagModelUi?.tagModel?.tagName
                    tagModelUi?.tagModel?.tagName?.let {
                        mainViewModel.getNewByCategory(
                            "us",
                            it
                        )
                    }

                } else {
                    mBinding.popularNewsText.setText(R.string.popular_news)
                    mainViewModel.getNew("ru")
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
        val isLiked = false
        for (article in articleList) {
            uiList.add(UiArticle(article, pref.getBoolean(article.title, isLiked)))
        }
        return uiList
    }

    override fun onCellClickListener(article: Article) {
        bundle = bundleOf("article" to article)
        bundle?.putInt("argInt", 1)
        view?.findNavController()?.navigate(R.id.action_mainFragment_to_detailFragment, bundle)
    }

    override fun onFavIconClickListener(uiArticle: UiArticle) {
        pref.edit().putBoolean(uiArticle.article.title, uiArticle.isLiked)
            .apply()
        Log.d("idTitle", uiArticle.article.title.toString())
        val entityArticle = articleToEntityArticle.map(uiArticle.article)

        if (uiArticle.isLiked) {
            mainViewModel.insertArticle(entityArticle)
            Log.d("Title", entityArticle.title.toString())
            Toast.makeText(requireContext(), "SAVED", Toast.LENGTH_SHORT).show()
            counter++
            sharedViewModel.setCounter(counter)
            pref.edit().putInt("Counter", counter).apply()
        } else {
            mainViewModel.deleteArticle(entityArticle.title.toString())
            Log.d("Title", entityArticle.title.toString())
            Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
            sharedViewModel.articles.observe(viewLifecycleOwner) { articles ->
                Log.d("ARTICLE_TAG", articles.toString())
                Log.d("ARTICLE_TAG", uiArticle.toString())
                if (!articles.contains(uiArticle.article)) {

                    if (counter > 0) counter--

                }
            }

            sharedViewModel.setCounter(counter)
            pref.edit().putInt("Counter", counter).apply()
        }

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

    override fun onTagClickListener(tagModelUi: TagModelUi) {
        mBinding.progressBar.visibility = View.VISIBLE
        var job: Job? = null

        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                job?.cancel()

                job = MainScope().launch {
                    if (tagModelUi.isClicked) {
                        mBinding.popularNewsText.text = tagModelUi.tagModel.tagName
                        for (tagUi in tagUiList) {
                            tagUi.isClicked = tagUi == tagModelUi
                            pref.edit().putBoolean(tagUi.tagModel.tagName, tagUi.isClicked).apply()
                        }
                        mainViewModel.setTagUiList(tagUiList)
                        delay(500L)
                        mainViewModel.getNewByCategory("us", tagModelUi.tagModel.tagName)
                    } else {
                        mBinding.popularNewsText.setText(R.string.popular_news)
                        for (tagUi in tagUiList) {
                            tagUi.isClicked = false
                            pref.edit().putBoolean(tagUi.tagModel.tagName, tagUi.isClicked).apply()
                        }
                        mainViewModel.setTagUiList(tagUiList)
                        delay(500L)
                        mainViewModel.getNew("ru")
                    }

                    observeArticle()
                }
            } else {
                recreateUi()
            }

        }
    }


    private fun createListTagModelUi(tagListModel: List<TagModel>): List<TagModelUi> {
        val tagUiList = arrayListOf<TagModelUi>()
        for (tag in tagListModel) {
            tagUiList.add(TagModelUi(tag, pref.getBoolean(tag.tagName, false)))
        }

        return tagUiList

    }

    private fun observeArticle() {

        mainViewModel.newLiveData.observe(viewLifecycleOwner) { newsModel ->
            mBinding.apply {
                progressBar.visibility = View.INVISIBLE
                popularNewsText.visibility = View.VISIBLE
                newsAdapter.visibility = View.VISIBLE
                buttonRetryConnection.visibility = View.INVISIBLE
                connectDescriptionText.visibility = View.INVISIBLE
                connectText.visibility = View.INVISIBLE
                imageView.visibility = View.INVISIBLE
            }
            articles = newsModel.articles
            uiListArticle = map(articles as MutableList<Article>)
            mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
            articleAdapter = ArticleAdapter(uiListArticle, this, this, this)
            mBinding.newsAdapter.adapter = articleAdapter
        }
        mainViewModel.errorMessage.observe(
            viewLifecycleOwner
        ) { errorMessage ->
            Toast.makeText(requireContext(), "Request error", Toast.LENGTH_SHORT).show()
            Log.d("CheckData", "Error: $errorMessage")

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

}


