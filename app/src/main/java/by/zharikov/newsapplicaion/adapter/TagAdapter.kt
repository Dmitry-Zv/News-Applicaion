package by.zharikov.newsapplicaion.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.TagModelUi
import by.zharikov.newsapplicaion.utils.TagClickListener
import kotlinx.android.synthetic.main.item_tag.view.*

class TagAdapter(
    private val tagUiList: List<TagModelUi>,
    private val tagClickListener: TagClickListener
) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    inner class TagViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tagItem = tagUiList[position]
        holder.itemView.apply {
            txt_tag_name.text = tagItem.tagModel.tagName
            img_tag.setImageResource(tagItem.tagModel.imageInt)
            if (tagItem.isClicked) {
                txt_tag_name.setBackgroundColor(Color.TRANSPARENT)

            } else {
                txt_tag_name.setBackgroundColor(Color.parseColor("#88000000"))
            }
            setOnClickListener {
                tagItem.isClicked = !tagItem.isClicked
                if (tagItem.isClicked) {
                    txt_tag_name.setBackgroundColor(Color.TRANSPARENT)

                } else {
                    txt_tag_name.setBackgroundColor(Color.parseColor("#88000000"))
                }
                tagClickListener.onTagClickListener(tagItem)


            }
        }

    }

    override fun getItemCount() = tagUiList.size
}