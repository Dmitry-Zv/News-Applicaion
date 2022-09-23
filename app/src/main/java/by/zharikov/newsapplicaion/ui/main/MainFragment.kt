package by.zharikov.newsapplicaion.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.ui.MyViewModelFactory
import by.zharikov.newsapplicaion.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!
    private var articles = emptyList<Article>()
    lateinit var articleAdapter: ArticleAdapter
    private lateinit var mainViewModel: MainFragmentViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.progressBar.visibility = View.VISIBLE
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        mainViewModel = ViewModelProvider(
            this, MyViewModelFactory(newsRepository = newsRepository)
        )[MainFragmentViewModel::class.java]
        mainViewModel.getNew("ru")
        mainViewModel.newLiveData.observe(viewLifecycleOwner, Observer { newsModel ->
            mBinding.progressBar.visibility = View.INVISIBLE

            mBinding.newsAdapter.layoutManager = LinearLayoutManager(requireContext())
            articles = newsModel.articles
            articleAdapter = ArticleAdapter(articles)
            mBinding.newsAdapter.adapter = articleAdapter
        })
        mainViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            mBinding.progressBar.visibility = View.INVISIBLE
            Log.d("CheckData", "Error: $errorMessage")

        })
//        CoroutineScope(Dispatchers.IO).launch {
//            val response = RetrofitNews.getApi().getTopHeadLines("ru", 1, Constants.API_KEY)
//            Log.d("CheckData", "${response}")
//            if (response.isSuccessful){
//                articles = response.body()?.articles!!
//            }
//
//        }


    }


}