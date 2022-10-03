package by.zharikov.newsapplicaion.ui.main

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
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.utils.ArticleToEntityArticle
import by.zharikov.newsapplicaion.utils.CellClickListener
import by.zharikov.newsapplicaion.utils.FavIconClickListener
import by.zharikov.newsapplicaion.utils.ShareIconClickListener

class MainFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener {

    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!
    private var articles = emptyList<Article>()
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var pref: SharedPreferences
    private val articleToEntityArticle = ArticleToEntityArticle()
    private lateinit var mainViewModel: MainFragmentViewModel
    private  var bundle: Bundle? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (bundle == null){
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.progressBar.visibility = View.VISIBLE
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        pref = requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                newsRepository = newsRepository,
                articleEntityRepository = articleEntityRepository
            )
        )[MainFragmentViewModel::class.java]
        mainViewModel.getNew("ru")
        mainViewModel.newLiveData.observe(viewLifecycleOwner, Observer { newsModel ->
            mBinding.progressBar.visibility = View.INVISIBLE
            articles = newsModel.articles
            val uiListArticle = map(articles as MutableList<Article>)
            articleAdapter = ArticleAdapter(uiListArticle, this, this, this)
            mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
            mBinding.newsAdapter.adapter = articleAdapter
        })
        mainViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            mBinding.progressBar.visibility = View.INVISIBLE
            Log.d("CheckData", "Error: $errorMessage")

        })


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
        } else {
            mainViewModel.deleteArticle(entityArticle.title.toString())
            Log.d("Title", entityArticle.title.toString())
            Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
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
}


