package by.zharikov.newsapplicaion.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.utils.ArticlesDiffUtil
import by.zharikov.newsapplicaion.utils.CellClickListener
import by.zharikov.newsapplicaion.utils.FavIconClickListener
import by.zharikov.newsapplicaion.utils.ShareIconClickListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_article.view.*

class ArticleAdapter(
    private var articles: MutableList<UiArticle>,
    private val cellClickListener: CellClickListener,
    private val favIconClickListener: FavIconClickListener,
    private val shareIconClickListener: ShareIconClickListener
) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val uiArticle = articles[position]
        holder.itemView.apply {
            if (uiArticle.article.urlToImage == null) article_image.setImageResource(R.drawable.news)
            else Glide.with(this).load(uiArticle.article.urlToImage).into(article_image)
            article_image.clipToOutline = true
            article_date.text = uiArticle.article.publishedAt
            article_title.text = uiArticle.article.title
            setOnClickListener {
                cellClickListener.onCellClickListener(uiArticle.article)
            }
            if (uiArticle.isLiked) {
                icon_favourite.setImageResource(R.drawable.ic_favorite_24)
                Log.d("Bool", "${uiArticle.isLiked}")
            } else {
                icon_favourite.setImageResource(R.drawable.ic_favorite_border_24)
                Log.d("Bool", "${uiArticle.isLiked}")
            }
            icon_favourite.setOnClickListener {
                uiArticle.isLiked = !uiArticle.isLiked
                favIconClickListener.onFavIconClickListener(uiArticle = uiArticle)
                if (uiArticle.isLiked) {
                    icon_favourite.setImageResource(R.drawable.ic_favorite_24)
                    Log.d("Bool", "${uiArticle.isLiked}")
                } else {
                    icon_favourite.setImageResource(R.drawable.ic_favorite_border_24)
                    Log.d("Bool", "${uiArticle.isLiked}")
                }
            }
            icon_share.setOnClickListener {
                shareIconClickListener.onShareIconClickListener(uiArticle.article.url.toString())
            }

        }
    }

    fun updateArticles(articles: MutableList<UiArticle>) {
        val articlesDiffUtil = ArticlesDiffUtil(this.articles, articles)
        val diffResult = DiffUtil.calculateDiff(articlesDiffUtil)
        this.articles.clear()
        this.articles = articles
        diffResult.dispatchUpdatesTo(this)

    }

    override fun getItemCount() = articles.size


}