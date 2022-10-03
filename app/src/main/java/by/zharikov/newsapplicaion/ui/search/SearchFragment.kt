package by.zharikov.newsapplicaion.ui.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.FragmentSearchBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.utils.ArticleToEntityArticle
import by.zharikov.newsapplicaion.utils.CellClickListener
import by.zharikov.newsapplicaion.utils.FavIconClickListener
import by.zharikov.newsapplicaion.utils.ShareIconClickListener

class SearchFragment : Fragment(), CellClickListener, FavIconClickListener, ShareIconClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val mBinding get() = _binding!!
    lateinit var articles: List<Article>
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var entityArticle: EntityArticle
    private lateinit var pref: SharedPreferences
    private lateinit var articleAdapter: ArticleAdapter
    private val articleToEntityArticle = ArticleToEntityArticle()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        pref = requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
        mBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mBinding.searchProgressBar.visibility = View.VISIBLE
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchViewModel = ViewModelProvider(
                    this@SearchFragment,
                    SearchViewModelFactory(newsRepository, articleEntityRepository)
                )[SearchViewModel::class.java]

                searchViewModel.getNews(p0.toString())
                searchViewModel.newsModel.observe(viewLifecycleOwner, Observer { response ->
                    mBinding.searchProgressBar.visibility = View.INVISIBLE
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


                })
                searchViewModel.errorMessage.observe(viewLifecycleOwner, Observer { response ->
                    Log.d("CheckData", "Error: $response")
                })
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
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
        } else {
            searchViewModel.deleteArticle(entityArticle.title.toString())
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


