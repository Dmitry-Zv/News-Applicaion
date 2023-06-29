package by.zharikov.newsapplicaion.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.adapter.TagAdapter
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.databinding.FragmentMainBinding
import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.presentation.SharedViewModel
import by.zharikov.newsapplicaion.presentation.SharedViewModelEvent
import by.zharikov.newsapplicaion.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : Fragment(), CellClickListener, SaveIconClickListener, ShareIconClickListener,
    TagClickListener {

    private var _binding: FragmentMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var tagAdapter: TagAdapter


    private val viewModel: MainFragmentViewModel by viewModels()

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
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSetting.setUpToolBar("News", Constants.FRAGMENT_MAIN)

        sharedViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == MyState.Fetched) {
                viewModel.onEvent(event = MainFragmentEvent.GetArticles("us"))

            } else {
                recreateUi()
            }
        }
        collectLatestLifecycleFlow(viewModel.tagUiList) { tagUiList ->
            tagAdapter = TagAdapter(tagUiList, this@MainFragment)
            mBinding.recyclerTags.adapter = tagAdapter
            mBinding.lblExplore.text = requireContext().getString(R.string.explore)


        }

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state.state) {
                MainFragmentStateName.DEFAULT.name -> {

                }
                MainFragmentStateName.REFRESH.name -> {
                    view.findNavController().navigate(R.id.action_mainFragment_self)
                }
                MainFragmentStateName.SHARED_ARTICLE.name -> {
                    state.url?.let {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, it)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    } ?: viewModel.onEvent(MainFragmentEvent.ShowError("No url provided..."))

                }
                MainFragmentStateName.DETAILED.name -> {

                    state.uiArticle?.let { uiArticle ->
                        val bundle = bundleOf("uiArticle" to uiArticle)
                        view.findNavController()
                            .navigate(R.id.action_mainFragment_to_detailFragment, bundle)

                    }
                        ?: viewModel.onEvent(event = MainFragmentEvent.ShowError("No article provided..."))
                }
                MainFragmentStateName.GET_ARTICLES.name -> {
                    when (state.isLoading) {
                        true -> mBinding.progressBar.visibility = View.VISIBLE
                        false -> mBinding.progressBar.visibility = View.GONE
                    }
                    when {
                        state.data.isEmpty() && state.error != null -> {
                            showSnackBar(view = mBinding.root, msg = state.error)
                        }
                        state.data.isNotEmpty() -> {

                            observeArticle(uiArticles = state.data)
                        }
                    }
                }
                MainFragmentStateName.ERROR.name -> {

                    showSnackBar(view = mBinding.root, msg = state.error ?: "Unknown error")
                }
            }
        }




        mBinding.buttonRetryConnection.setOnClickListener {
            viewModel.onEvent(event = MainFragmentEvent.Refresh)
        }
        mBinding.refreshLayout.setOnRefreshListener {
            viewModel.onEvent(event = MainFragmentEvent.Refresh)
        }


    }


    override fun onCellClickListener(uiArticle: UiArticle) {

        viewModel.onEvent(event = MainFragmentEvent.OnCellClick(uiArticle = uiArticle))
    }

    override fun onSaveIconClickListener(uiArticle: UiArticle) {

        viewModel.onEvent(
            event = MainFragmentEvent.OnSaveIconClick(
                uiArticle = uiArticle
            )
        )
        sharedViewModel.onEvent(event = SharedViewModelEvent.ShowArticlesBadgeCounter(uiArticle = uiArticle))

    }

    override fun onShareIconClickListener(url: String) {
        viewModel.onEvent(event = MainFragmentEvent.OnShareIconClick(url = url))

    }

    override fun onTagClickListener(tagUi: TagModelUi) {
        viewModel.onEvent(event = MainFragmentEvent.OnTagClick(tagModelUi = tagUi))
    }

    private fun observeArticle(uiArticles: List<UiArticle>) {
        articleAdapter = ArticleAdapter(this, this, this)
        with(mBinding) {
            newsAdapter.adapter = articleAdapter
            newsAdapter.layoutManager = LinearLayoutManager(requireContext())
        }
        articleAdapter.setData(uiArticles = uiArticles)

        mBinding.apply {
            popularNewsText.visibility = View.VISIBLE
            newsAdapter.visibility = View.VISIBLE
            recyclerTags.visibility = View.VISIBLE
            buttonRetryConnection.visibility = View.GONE
            connectDescriptionText.visibility = View.GONE
            connectText.visibility = View.GONE
            imageView.visibility = View.GONE
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

    override fun onStop() {
        super.onStop()
        viewModel.onEvent(event = MainFragmentEvent.Default)
    }


}


