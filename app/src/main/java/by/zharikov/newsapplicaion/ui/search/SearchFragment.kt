package by.zharikov.newsapplicaion.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.api.RetrofitNews
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.databinding.FragmentSearchBinding
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.ui.MyViewModelFactory
import by.zharikov.newsapplicaion.utils.CellClickListener

class SearchFragment : Fragment(), CellClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val retrofitNews = RetrofitNews()
        val newsRepository = NewsRepository(retrofitNews)
        mBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mBinding.searchProgressBar.visibility = View.VISIBLE
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchViewModel = ViewModelProvider(
                    this@SearchFragment,
                    SearchViewModelFactory(newsRepository)
                )[SearchViewModel::class.java]

                searchViewModel.getNews(p0.toString())
                searchViewModel.newsModel.observe(viewLifecycleOwner, Observer { response ->
                    mBinding.searchProgressBar.visibility = View.INVISIBLE
                    articleAdapter =
                        ArticleAdapter(response.articles, requireContext(), this@SearchFragment)
                    mBinding.searchRecycler.apply {
                        adapter = articleAdapter
                        layoutManager = LinearLayoutManager(requireContext())
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
        view?.findNavController()?.navigate(R.id.action_searchFragment_to_detailFragment, bundle)
    }


}