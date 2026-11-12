package com.example.belltest.featured

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.belltest.featured.databinding.FeaturedItemVideoBinding

internal class FeaturedVideoAdapter(
    private val cards: List<FeaturedCard>,
    private val onWatch: (FeaturedCard) -> Unit,
) : RecyclerView.Adapter<FeaturedVideoAdapter.FeaturedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder =
        FeaturedViewHolder(
            FeaturedItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(cards[position], position)
    }

    override fun getItemCount(): Int = cards.size

    inner class FeaturedViewHolder(
        private val binding: FeaturedItemVideoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: FeaturedCard, position: Int) {
            binding.videoPreview.setBackgroundColor(card.accentColor.toInt())
            binding.previewNumber.text = (position + 1).toString().padStart(2, '0')
            binding.videoAuthor.text = card.author
            binding.videoTitle.text = card.title
            binding.videoQuality.text = "${card.qualityLabel}  ${card.durationLabel}"
            binding.videoTags.text = card.tags.joinToString(separator = "  ") { "#$it" }
            binding.videoSource.text = "${card.popularity} · ${card.sourceHost}"
            binding.watchButton.setOnClickListener { onWatch(card) }
            binding.root.setOnClickListener { onWatch(card) }
        }
    }
}
