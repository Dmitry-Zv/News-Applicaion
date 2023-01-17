package by.zharikov.newsapplicaion.ui.favourie

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.ArticleAdapter
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.databinding.CustomToolBarLayoutBinding
import by.zharikov.newsapplicaion.databinding.FragmentFavourieBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.ArticlePreferencesRepository
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModel
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.usecase.UiState
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesUseCase
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModelFactory
import by.zharikov.newsapplicaion.utils.*
import by.zharikov.newsapplicaion.worker.UploadWorker


class FavouriteFragment : Fragment(), CellClickListener, FavIconClickListener,
    ShareIconClickListener {

    private var _binding: FragmentFavourieBinding? = null
    private val mBinding get() = _binding!!
    private val prefCounter: SharedPreferences by lazy {
        requireContext().getSharedPreferences("COUNTER_SHARED_PREF", Context.MODE_PRIVATE)
    }
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private lateinit var articleAdapter: ArticleAdapter

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var toolBarSetting: ToolBarSetting
    private var _customToolBarLayoutBinding: CustomToolBarLayoutBinding? = null
    private lateinit var viewModel: ArticlePreferencesViewModel


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
        _binding = FragmentFavourieBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _customToolBarLayoutBinding = CustomToolBarLayoutBinding.inflate(layoutInflater)
        toolBarSetting.setUpToolBar("Favourite", Constants.FRAGMENT_FAVOURITE)
        val articleEntityRepository = ArticleEntityRepository(requireContext())
        val articlePreferencesRepository = ArticlePreferencesRepository(pref)
        val articlePreferencesUseCase =
            ArticlePreferencesUseCase(articlePreferencesRepository, articleEntityRepository)
        viewModel = ViewModelProvider(
            this,
            ArticlePreferencesViewModelFactory(articlePreferencesUseCase)
        )[ArticlePreferencesViewModel::class.java]
        val counter = 0
        sharedViewModel.setCounter(counter)
        prefCounter.edit().putInt("Counter", counter).apply()

        viewModel.getAllArticles()

        collectLatestLifecycleFlow(viewModel.uiStateFlow) { uiState ->
            when (uiState) {

                is UiState.ShowEmptyScreen -> {
                    sharedViewModel.setCountItemFav(0)
                    with(mBinding) {
                        recyclerFavourite.visibility = View.INVISIBLE
                        favouriteImage.visibility = View.VISIBLE
                        favouriteTitle.visibility = View.VISIBLE
                        favouriteDescriptionText.visibility = View.VISIBLE
                    }
                }
                is UiState.ShowUiArticle -> {

                    with(mBinding) {
                        recyclerFavourite.visibility = View.VISIBLE
                        favouriteImage.visibility = View.INVISIBLE
                        favouriteTitle.visibility = View.INVISIBLE
                        favouriteDescriptionText.visibility = View.INVISIBLE

                    }
                    sharedViewModel.setCountItemFav(uiState.uiArticles.size)
                    Log.d("UI_ARTICLE_SIZE", uiState.uiArticles.size.toString())
                    articleAdapter =
                        ArticleAdapter(
                            uiState.uiArticles,
                            this@FavouriteFragment,
                            this@FavouriteFragment,
                            this@FavouriteFragment
                        )
                    mBinding.recyclerFavourite.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = articleAdapter

                    }
                }
                else -> {}
            }

        }

    }


    override fun onCellClickListener(article: Article) {
        val bundle = bundleOf("article" to article)
        bundle.putInt("argInt", 3)
        view?.findNavController()
            ?.navigate(R.id.action_favouriteFragment_to_detailFragment, bundle)
    }

    override fun onFavIconClickListener(uiArticle: UiArticle) {
        viewModel.deleteEntityArticle(uiArticle.article)
        viewModel.getAllArticles()
        setOnTimeWorkRequest()


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
}


