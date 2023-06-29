package by.zharikov.newsapplicaion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ItemArticleBinding
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.utils.CellClickListener
import by.zharikov.newsapplicaion.utils.SaveIconClickListener
import by.zharikov.newsapplicaion.utils.ShareIconClickListener
import com.bumptech.glide.Glide

class ArticleAdapter(
    private val cellClickListener: CellClickListener,
    private val saveIconClickListener: SaveIconClickListener,
    private val shareIconClickListener: ShareIconClickListener
) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val callback = object : DiffUtil.ItemCallback<UiArticle>() {
        override fun areItemsTheSame(oldItem: UiArticle, newItem: UiArticle): Boolean {
            return oldItem.article.url == newItem.article.url
        }

        override fun areContentsTheSame(oldItem: UiArticle, newItem: UiArticle): Boolean {
            return oldItem == newItem
        }

    }


    private val listDiffer = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val uiArticle = listDiffer.currentList[position]
        holder.binding.apply {
            if (uiArticle.article.urlToImage == null) articleImage.setImageResource(R.drawable.news)
            else Glide.with(this.root).load(uiArticle.article.urlToImage).into(articleImage)
            articleImage.clipToOutline = true
            articleDate.text = uiArticle.article.publishedAt
            articleTitle.text = uiArticle.article.title
            root.setOnClickListener {
                cellClickListener.onCellClickListener(uiArticle = uiArticle)
            }
            if (uiArticle.isSave) iconFavourite.setImageResource(R.drawable.ic_favorite_24)
            else iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
            iconFavourite.setOnClickListener {
                saveIconClickListener.onSaveIconClickListener(uiArticle = uiArticle)
                uiArticle.isSave = !uiArticle.isSave
                if (uiArticle.isSave) iconFavourite.setImageResource(R.drawable.ic_favorite_24)
                else iconFavourite.setImageResource(R.drawable.ic_favorite_border_24)
            }
            iconShare.setOnClickListener {
                shareIconClickListener.onShareIconClickListener(url = uiArticle.article.url.toString())
            }
        }

    }


    override fun getItemCount() = listDiffer.currentList.size

    fun setData(uiArticles: List<UiArticle>) = listDiffer.submitList(uiArticles)
}