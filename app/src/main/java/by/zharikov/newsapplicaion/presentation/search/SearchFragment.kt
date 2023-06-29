package by.zharikov.newsapplicaion.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.databinding.FragmentSearchBinding
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.presentation.SharedViewModel
import by.zharikov.newsapplicaion.presentation.SharedViewModelEvent
import by.zharikov.newsapplicaion.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(), CellClickListener, SaveIconClickListener, ShareIconClickListener,
    MenuProvider {

    private var _binding: FragmentSearchBinding? = null
    private val mBinding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var toolBarSetting: ToolBarSetting

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSetting.setUpToolBar("Search", Constants.FRAGMENT_SEARCH)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)




        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state != MyState.Fetched) {
                recreateUi()
            }
        }

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state.state) {
                SearchFragmentStateName.DETAILED.name -> {
                    state.uiArticle?.let {
                        val bundle = bundleOf("uiArticle" to it)
                        bundle.putInt("argInt", 2)
                        view.findNavController()
                            .navigate(R.id.action_searchFragment_to_detailFragment, bundle)
                    }
                        ?: viewModel.onEvent(event = SearchFragmentEvent.ShowError(msg = "Article is null..."))

                }
                SearchFragmentStateName.SHARED_ARTICLE.name -> {
                    state.url?.let {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, it)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                }
                SearchFragmentStateName.REFRESH.name -> {
                    view.findNavController().navigate(R.id.action_searchFragment_self)
                }
                SearchFragmentStateName.ERROR.name -> {
                    showSnackBar(msg = state.error ?: "Unknown error", view = mBinding.root)
                }
                SearchFragmentStateName.GET_ARTICLES.name -> {
                    when (state.isLoading) {
                        true -> mBinding.searchProgressBar.visibility = View.VISIBLE
                        false -> mBinding.searchProgressBar.visibility = View.GONE
                    }
                    observeArticle(state.data)
                }
            }
        }





        mBinding.buttonRetryConnection.setOnClickListener {
            viewModel.onEvent(event = SearchFragmentEvent.Refresh)

        }
    }

    private fun observeArticle(uiArticles: List<UiArticle>) {

        articleAdapter = ArticleAdapter(this, this, this)
        with(mBinding) {
            searchRecycler.adapter = articleAdapter
            searchRecycler.layoutManager = LinearLayoutManager(requireContext())
            searchRecycler.visibility = View.VISIBLE
            buttonRetryConnection.visibility = View.INVISIBLE
            imageView.visibility = View.INVISIBLE
            connectText.visibility = View.INVISIBLE
            connectDescriptionText.visibility = View.INVISIBLE
        }
        articleAdapter.setData(uiArticles = uiArticles)
    }


    override fun onCellClickListener(uiArticle: UiArticle) {
        viewModel.onEvent(event = SearchFragmentEvent.OnCellClick(uiArticle = uiArticle))

    }


    override fun onSaveIconClickListener(uiArticle: UiArticle) {

        viewModel.onEvent(
            event = SearchFragmentEvent.OnSaveIconClick(
                uiArticle = uiArticle
            )
        )
        sharedViewModel.onEvent(event = SharedViewModelEvent.ShowArticlesBadgeCounter(uiArticle = uiArticle))

    }


    override fun onShareIconClickListener(url: String) {
        viewModel.onEvent(event = SearchFragmentEvent.OnShareIconClick(url = url))

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
                p0?.let {
                    if (it.isNotBlank()) viewModel.onEvent(
                        event = SearchFragmentEvent.GetArticles(
                            query = it
                        )
                    )
                }
                return false
            }

        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return true
    }

    private fun recreateUi() {
        mBinding.apply {
            searchProgressBar.visibility = View.INVISIBLE
            searchRecycler.visibility = View.INVISIBLE
            buttonRetryConnection.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            connectText.visibility = View.VISIBLE
            connectDescriptionText.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onEvent(event = SearchFragmentEvent.Default)
    }

}


