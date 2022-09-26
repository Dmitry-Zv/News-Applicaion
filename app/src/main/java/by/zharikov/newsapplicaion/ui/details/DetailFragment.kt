package by.zharikov.newsapplicaion.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.FragmentDetailBinding
import com.bumptech.glide.Glide


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val mBinding get() = _binding!!
    private val bundleArgs: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val articleArg = bundleArgs.article
        articleArg.let { article ->
            article.urlToImage.let {
                Glide.with(this).load(article.urlToImage).into(mBinding.headerImage)
            }
            mBinding.headerImage.clipToOutline = true
            mBinding.articleDetailTitleText.text = article.title
            mBinding.articleDetailDescriptionText.text = article.description
            mBinding.articleDetailButton.setOnClickListener {
                try {
                    val address = Uri.parse(article.url)
                    val linkIntent = Intent(Intent.ACTION_VIEW, address)
                    startActivity(linkIntent)
                } catch (e: Exception) {
                    Log.d("CheckData", "The device doesn't have any browsers! ${e.message}")
                }

            }
        }

    }


}