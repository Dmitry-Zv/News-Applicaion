package by.zharikov.newsapplicaion.ui.search

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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.FragmentSearchBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.utils.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val mBinding get() = _binding!!
    lateinit var articles: List<Article>
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var entityArticle: EntityArticle
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private lateinit var articleAdapter: ArticleAdapter
    private val articleToEntityArticle = ArticleToEntityArticle()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("isConnected", "onViewCreated")
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        counter = pref.getInt("Counter", 0)
        searchViewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(newsRepository, articleEntityRepository)
        )[SearchViewModel::class.java]


        var job: Job? = null
        mBinding.edSearch.addTextChangedListener { text ->
            sharedViewModel.state.observe(viewLifecycleOwner) { state ->
                if (state == MyState.Fetched) {

                    job?.cancel()
                    if (text != null) {
                        if (text.isNotEmpty())
                            mBinding.searchProgressBar.visibility = View.VISIBLE
                    }
                    job = MainScope().launch {
                        delay(500L)
                        text?.let {
                            if (it.toString().isNotEmpty()) {
                                searchViewModel.getNews(it.toString())

                                searchViewModel.newsModel.observe(viewLifecycleOwner) { response ->
                                    mBinding.apply {
                                        searchProgressBar.visibility = View.INVISIBLE
                                        searchRecycler.visibility = View.VISIBLE
                                        buttonRetryConnection.visibility = View.INVISIBLE
                                        imageView.visibility = View.INVISIBLE
                                        connectText.visibility = View.INVISIBLE
                                        connectDescriptionText.visibility = View.INVISIBLE
                                    }
                                    articles = response.articles
                                    val uiArticles = map(articles as MutableList<Article>)
                                    articleAdapter =
                                        ArticleAdapter(
                                            uiArticles,
                                            this@SearchFragment,
                                            this@SearchFragment,
                                            this@SearchFragment
                                        )
                                    mBinding.searchRecycler.apply {
                                        layoutManager = LinearLayoutManager(requireContext())
                                        adapter = articleAdapter
                                    }


                                }
                                searchViewModel.errorMessage.observe(viewLifecycleOwner) { response ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Request error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("CheckData", "Error: $response")
                                }

                            }
                        }

                    }

                } else {
                    mBinding.apply {
                        searchProgressBar.visibility = View.INVISIBLE
                        searchRecycler.visibility = View.INVISIBLE
                        buttonRetryConnection.visibility = View.VISIBLE
                        imageView.visibility = View.VISIBLE
                        connectText.visibility = View.VISIBLE
                        connectDescriptionText.visibility = View.VISIBLE
                    }
                }
            }
        }

        mBinding.buttonRetryConnection.setOnClickListener {
            view.findNavController().navigate(R.id.action_searchFragment_self)
        }
    }


    override fun onCellClickListener(article: Article) {
        val bundle = bundleOf("article" to article)
        bundle.putInt("argInt", 2)
        view?.findNavController()?.navigate(R.id.action_searchFragment_to_detailFragment, bundle)
    }

    private fun map(articleList: MutableList<Article>): MutableList<UiArticle> {
        val uiList = mutableListOf<UiArticle>()
        val isLiked = false
        for (article in articleList) {
            uiList.add(UiArticle(article, pref.getBoolean(article.title, isLiked)))
        }
        return uiList
    }


    override fun onFavIconClickListener(uiArticle: UiArticle) {
        pref.edit().putBoolean(uiArticle.article.title, uiArticle.isLiked)
            .apply()
        Log.d("idTitle", uiArticle.article.title.toString())
        entityArticle = articleToEntityArticle.map(uiArticle.article)
        if (uiArticle.isLiked) {
            searchViewModel.insertArticle(entityArticle)
            Toast.makeText(requireContext(), "SAVED", Toast.LENGTH_SHORT).show()
            counter++
            sharedViewModel.setCounter(counter)
            pref.edit().putInt("Counter", counter).apply()
        } else {
            searchViewModel.deleteArticle(entityArticle.title.toString())
            Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
            sharedViewModel.articles.observe(viewLifecycleOwner) { articles ->
                if (!articles.contains(uiArticle.article)) {
                    if (counter > 0) counter--
                }
            }
        }

        sharedViewModel.setCounter(counter)
        pref.edit().putInt("Counter", counter).apply()
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

}


