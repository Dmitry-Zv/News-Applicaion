package by.zharikov.newsapplicaion.presentation.details

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
import androidx.navigation.fragment.navArgs
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.FragmentDetailBinding
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.presentation.SharedViewModel
import by.zharikov.newsapplicaion.presentation.SharedViewModelEvent
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.ToolBarSetting
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import by.zharikov.newsapplicaion.utils.showSnackBar
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val mBinding get() = _binding!!
    private val bundleArgs: DetailFragmentArgs by navArgs()
    private lateinit var toolBarSetting: ToolBarSetting

    private val viewModel: DetailViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSetting = context as ToolBarSetting
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSetting.setUpToolBar("Detail", Constants.FRAGMENT_DETAILED)

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state.state) {
                DetailFragmentStateName.DEFAULT.name -> {}

                DetailFragmentStateName.WEB.name -> {
                    state.article?.let {
                        val bundle = bundleOf("article_arg" to it)
                        view.findNavController()
                            .navigate(R.id.action_detailFragment_to_webFragment, bundle)
                    }
                        ?: viewModel.onEvent(event = DetailFragmentEvent.ShowError(msg = "Article is null..."))

                }
                DetailFragmentStateName.BACK.name -> {
                    view.findNavController().popBackStack()
                }
                DetailFragmentStateName.SHARED_ARTICLE.name -> {
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
                DetailFragmentStateName.ERROR.name -> {
                    showSnackBar(view = mBinding.root, msg = state.error ?: "Unknown error")
                }
                DetailFragmentStateName.SAVED.name -> {
                    when (state.isSaved) {
                        true -> mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_24)
                        false -> mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
                    }
                }
            }
        }

        val articleArg: UiArticle = bundleArgs.uiArticle
        articleArg.let { uiArticle ->

            uiArticle.article.urlToImage?.let {
                Glide.with(this).load(it).into(mBinding.headerImage)
            } ?: mBinding.headerImage.setImageResource(R.drawable.news)
            mBinding.headerImage.clipToOutline = true
            mBinding.articleDetailTitleText.text = uiArticle.article.title
            mBinding.articleDetailDescriptionText.text = uiArticle.article.description
            if (uiArticle.isSave) mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_24)
            else mBinding.iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
            mBinding.articleDetailButton.setOnClickListener {
                viewModel.onEvent(event = DetailFragmentEvent.PressWeb(article = uiArticle.article))


            }
            mBinding.iconShare.setOnClickListener {
                uiArticle.article.url?.let {
                    viewModel.onEvent(event = DetailFragmentEvent.OnShareIconClick(it))
                }
                    ?: viewModel.onEvent(event = DetailFragmentEvent.ShowError(msg = "Url no provided..."))


            }
            mBinding.iconBack.setOnClickListener {
                viewModel.onEvent(event = DetailFragmentEvent.PressBack)


            }
            mBinding.iconFavourite.setOnClickListener {
                viewModel.onEvent(event = DetailFragmentEvent.PressSaveIcon(uiArticle = uiArticle))
                sharedViewModel.onEvent(
                    event = SharedViewModelEvent.ShowArticlesBadgeCounter(
                        uiArticle = uiArticle
                    )
                )
            }

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.onEvent(event = DetailFragmentEvent.Default)
    }


}