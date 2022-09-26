package by.zharikov.newsapplicaion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.utils.CellClickListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_article.view.*

class ArticleAdapter(
    private val articles: List<Article>,
    private val context: Context,
    private val cellClickListener: CellClickListener
) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.itemView.apply {
            Glide.with(this).load(article.url).into(article_image)
            article_date.text = article.publishedAt
            article_title.text = article.title
            setOnClickListener {
                cellClickListener.onCellClickListener(article)
            }
        }
    }

    override fun getItemCount() = articles.size
}