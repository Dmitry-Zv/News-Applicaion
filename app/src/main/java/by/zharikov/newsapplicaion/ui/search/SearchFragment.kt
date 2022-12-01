package by.zharikov.newsapplicaion.ui.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import by.zharikov.newsapplicaion.worker.UploadWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener,
    MenuProvider {

    private var _binding: FragmentSearchBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var articles: List<Article>
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var entityArticle: EntityArticle
    private var uiArticles = mutableListOf<UiArticle>()
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private lateinit var articleAdapter: ArticleAdapter
    private val articleToEntityArticle = ArticleToEntityArticle()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var counter: Int = 0
    private lateinit var toolBarSetting: ToolBarSetting

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting
    }

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
        toolBarSetting.setUpToolBar("Search", Constants.FRAGMENT_SEARCH)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)



        Log.d("isConnected", "onViewCreated")
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        counter = pref.getInt("Counter", 0)
        searchViewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(newsRepository, articleEntityRepository)
        )[SearchViewModel::class.java]
        articleAdapter =
            ArticleAdapter(
                uiArticles,
                this,
                this,
                this
            )
        mBinding.searchRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
        mBinding.searchProgressBar.visibility = View.VISIBLE
        var job: Job? = null
        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                job?.cancel()

                job = MainScope().launch {

                    delay(500)
                    searchViewModel.getArticle("us")
                    observeArticle()
                }

            } else {
                recreateUi()
            }
        }




        mBinding.buttonRetryConnection.setOnClickListener {
            view.findNavController().navigate(R.id.action_searchFragment_self)
        }
    }

    private fun observeArticle() {
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
            uiArticles = map(articles as MutableList<Article>)
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
        setOnTimeWorkRequest()
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

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.tool_bar_menu, menu)
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "Type here to search"
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                var job: Job? = null
                sharedViewModel.state.observe(viewLifecycleOwner) { state ->
                    if (state == MyState.Fetched) {

                        job?.cancel()
                        if (p0 != null) {
                            if (p0.isNotEmpty())
                                mBinding.searchProgressBar.visibility = View.VISIBLE
                        }
                        job = MainScope().launch {
                            delay(500L)
                            p0?.let {
                                if (it.isNotEmpty()) {
                                    searchViewModel.getNews(it)

                                    observeArticle()

                                }
                            }

                        }

                    } else {
                        recreateUi()
                    }
                }
                return false
            }

        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return true
    }

    fun recreateUi() {
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


