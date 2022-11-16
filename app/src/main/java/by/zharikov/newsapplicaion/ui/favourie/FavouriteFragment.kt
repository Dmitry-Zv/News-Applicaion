package by.zharikov.newsapplicaion.ui.favourie

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.FragmentFavourieBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.utils.*


class FavouriteFragment : Fragment(), CellClickListener, FavIconClickListener,
    ShareIconClickListener {

    private var _binding: FragmentFavourieBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var favouriteViewModel: FavouriteViewModel
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var entityArticle: EntityArticle
    private var uiArticles = mutableListOf<UiArticle>()
    private val articleToEntityArticle = ArticleToEntityArticle()
    private val entityArticleToArticle = EntityArticleToArticle()
    private val sharedViewModel: SharedViewModel by activityViewModels()


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
        _binding = FragmentFavourieBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        val counter = 0
        sharedViewModel.setCounter(counter)
        pref.edit().putInt("Counter", counter).apply()
        favouriteViewModel = ViewModelProvider(
            this,
            FavouriteViewModelFactory(articleEntityRepository)
        )[FavouriteViewModel::class.java]
        articleAdapter =
            ArticleAdapter(uiArticles, this@FavouriteFragment, this@FavouriteFragment, this)
        mBinding.recyclerFavourite.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
        favouriteViewModel.getArticles()
        favouriteViewModel.saveData.observe(viewLifecycleOwner) { entityArticles ->
            for (entity in entityArticles) {
                Log.d("ChEntity", entity.title.toString())
            }
            val articles = mapFromEntityToArticle(entityArticles)
            uiArticles = map(articles as MutableList<Article>).asReversed()
            sharedViewModel.setUiListArticle(articles)
            mBinding.headerCount.text = uiArticles.size.toString()
            articleAdapter =
                ArticleAdapter(uiArticles, this@FavouriteFragment, this@FavouriteFragment, this)
            mBinding.recyclerFavourite.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = articleAdapter
            }


            if (entityArticles.isEmpty()) {
                mBinding.apply {
                    favouriteTitleHeader.visibility = View.INVISIBLE
                    headerCount.visibility = View.INVISIBLE
                    recyclerFavourite.visibility = View.INVISIBLE
                    favouriteImage.visibility = View.VISIBLE
                    favouriteTitle.visibility = View.VISIBLE
                    favouriteDescriptionText.visibility = View.VISIBLE
                }

            } else {
                mBinding.apply {
                    favouriteTitleHeader.visibility = View.VISIBLE
                    headerCount.visibility = View.VISIBLE
                    recyclerFavourite.visibility = View.VISIBLE
                    favouriteImage.visibility = View.INVISIBLE
                    favouriteTitle.visibility = View.INVISIBLE
                    favouriteDescriptionText.visibility = View.INVISIBLE
                }
            }

        }


    }


    private fun map(articles: MutableList<Article>): MutableList<UiArticle> {
        val uiList = mutableListOf<UiArticle>()
        val isLiked = false
        for (article in articles) {
            uiList.add(UiArticle(article, pref.getBoolean(article.title, isLiked)))
        }
        return uiList
    }

    private fun mapFromEntityToArticle(entityArticles: List<EntityArticle>): List<Article> {
        val articles = arrayListOf<Article>()
        for (entity in entityArticles) {
            articles.add(entityArticleToArticle.map(entity))
        }
        return articles
    }

    override fun onCellClickListener(article: Article) {
        val bundle = bundleOf("article" to article)
        bundle.putInt("argInt", 3)
        view?.findNavController()?.navigate(R.id.action_favouriteFragment_to_detailFragment, bundle)
    }

    override fun onFavIconClickListener(uiArticle: UiArticle) {
        pref.edit().putBoolean(uiArticle.article.title, uiArticle.isLiked)
            .apply()
        Log.d("idTitle", uiArticle.article.title.toString())
        entityArticle = articleToEntityArticle.map(uiArticle.article)
        favouriteViewModel.deleteArticle(entityArticle.title.toString())
        favouriteViewModel.getArticles()
        Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
        favouriteViewModel.saveData.observe(viewLifecycleOwner) { entityArticles ->
            val articles = mapFromEntityToArticle(entityArticles)
            val uiArticles = map(articles as MutableList<Article>).asReversed()
            articleAdapter.updateArticles(uiArticles)

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


