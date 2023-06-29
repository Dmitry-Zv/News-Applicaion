package by.zharikov.newsapplicaion.presentation.favourie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.databinding.CustomToolBarLayoutBinding
import by.zharikov.newsapplicaion.databinding.FragmentFavourieBinding
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.presentation.SharedViewModel
import by.zharikov.newsapplicaion.presentation.SharedViewModelEvent
import by.zharikov.newsapplicaion.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteFragment : Fragment(), CellClickListener, SaveIconClickListener,
    ShareIconClickListener {

    private var _binding: FragmentFavourieBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private val viewModel: FavouriteViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var toolBarSetting: ToolBarSetting
    private var _customToolBarLayoutBinding: CustomToolBarLayoutBinding? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavourieBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _customToolBarLayoutBinding = CustomToolBarLayoutBinding.inflate(layoutInflater)
        toolBarSetting.setUpToolBar("Favourite", Constants.FRAGMENT_FAVOURITE)

        viewModel.onEvent(event = FavouriteFragmentEvent.GetArticle)
        sharedViewModel.onEvent(event = SharedViewModelEvent.ResetBadgeCounter)

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state.state) {
                FavouriteFragmentStateName.DETAILED.name -> {
                    state.uiArticle?.let {
                        val bundle = bundleOf("uiArticle" to it)
                        bundle.putInt("argInt", 3)
                        view.findNavController()
                            .navigate(R.id.action_favouriteFragment_to_detailFragment, bundle)
                    }
                        ?: viewModel.onEvent(event = FavouriteFragmentEvent.ShowError(msg = "Article is null..."))
                }
                FavouriteFragmentStateName.SHARED_ARTICLE.name -> {
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
                FavouriteFragmentStateName.ERROR.name -> {
                    showSnackBar(view = mBinding.root, msg = state.error ?: "Unknown error")
                }
                FavouriteFragmentStateName.GET_ARTICLES.name -> {
                    if (state.data.isEmpty()) {
                        sharedViewModel.onEvent(event = SharedViewModelEvent.CountItemFavourite(data = 0))
                        with(mBinding) {
                            recyclerFavourite.visibility = View.INVISIBLE
                            favouriteImage.visibility = View.VISIBLE
                            favouriteTitle.visibility = View.VISIBLE
                            favouriteDescriptionText.visibility = View.VISIBLE
                        }
                    } else {
                        sharedViewModel.onEvent(event = SharedViewModelEvent.CountItemFavourite(data = state.data.size))
                        articleAdapter =
                            ArticleAdapter(this, this, this)
                        articleAdapter.setData(state.data)
                        with(mBinding) {
                            recyclerFavourite.visibility = View.VISIBLE
                            favouriteImage.visibility = View.INVISIBLE
                            favouriteTitle.visibility = View.INVISIBLE
                            favouriteDescriptionText.visibility = View.INVISIBLE
                            recyclerFavourite.layoutManager = LinearLayoutManager(requireContext())
                            recyclerFavourite.adapter = articleAdapter
                        }
                    }

                }

            }

        }

    }


    override fun onCellClickListener(uiArticle: UiArticle) {
        viewModel.onEvent(event = FavouriteFragmentEvent.OnCellClick(uiArticle = uiArticle))

    }

    override fun onSaveIconClickListener(uiArticle: UiArticle) {
        uiArticle.article.let {
            viewModel.onEvent(
                event = FavouriteFragmentEvent.OnDeleteIcon(
                    title = it.title!!,
                    publishedAt = it.publishedAt!!
                )
            )

        }

    }

    override fun onShareIconClickListener(url: String) {
        viewModel.onEvent(event = FavouriteFragmentEvent.OnShareIconClick(url = url))

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.onEvent(event = FavouriteFragmentEvent.Default)
    }
}


