package com.m4ykey.ui.adapter.viewholder

import com.m4ykey.core.views.formatDate
import com.m4ykey.core.views.loadImage
import com.m4ykey.core.views.recyclerview.BaseViewHolder
import com.m4ykey.data.domain.model.Article
import com.m4ykey.ui.R
import com.m4ykey.ui.databinding.LayoutNewsListBinding
import com.m4ykey.ui.logos.logos

class NewsViewHolder(
    binding: LayoutNewsListBinding,
    private val onNewsClick : (Article) -> Unit
) : BaseViewHolder<Article, LayoutNewsListBinding>(binding) {

    override fun bind(item : Article) {
        with(binding) {
            linearLayoutArticle.setOnClickListener { onNewsClick(item) }

            val formatDate = formatDate(
                item.publishedAt,
                outputPattern = "dd MMM yyyy, HH:mm",
                inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
            )

            loadImage(imgArticle, item.urlToImage, imgArticle.context)
            loadImage(imgLogo, logos[item.source.name].orEmpty(), imgLogo.context)
            txtTitle.text = item.title
            txtSourceNameDate.text = txtSourceNameDate.context.getString(R.string.source_date, item.source.name, formatDate)
        }
    }
}