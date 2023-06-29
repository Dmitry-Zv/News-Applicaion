package by.zharikov.newsapplicaion.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.databinding.ItemTagBinding
import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.utils.TagClickListener

class TagAdapter(
    private val tagUiList: List<TagModelUi>,
    private val tagClickListener: TagClickListener
) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    inner class TagViewHolder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tagItem = tagUiList[position]
        holder.binding.apply {
            txtTagName.text = tagItem.tagModel.tagName
            imgTag.setImageResource(tagItem.tagModel.imageInt)
            if (tagItem.isClicked) {
                txtTagName.setBackgroundColor(Color.TRANSPARENT)

            } else {
                txtTagName.setBackgroundColor(Color.parseColor("#88000000"))
            }
            root.setOnClickListener {
                tagItem.isClicked = !tagItem.isClicked
                if (tagItem.isClicked) {
                    txtTagName.setBackgroundColor(Color.TRANSPARENT)

                } else {
                    txtTagName.setBackgroundColor(Color.parseColor("#88000000"))
                }
                tagClickListener.onTagClickListener(tagItem)


            }
        }

    }

    override fun getItemCount() = tagUiList.size
}