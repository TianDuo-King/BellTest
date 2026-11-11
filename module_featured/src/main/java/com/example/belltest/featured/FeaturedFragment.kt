package com.example.belltest.featured

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belltest.base.BaseViewBindingFragment
import com.example.belltest.featured.databinding.FeaturedFragmentBinding
import com.example.belltest.model.DemoVideoCatalog

class FeaturedFragment : BaseViewBindingFragment<FeaturedFragmentBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FeaturedFragmentBinding =
        FeaturedFragmentBinding.inflate(inflater, container, false)

    override fun onBindingCreated(binding: FeaturedFragmentBinding, savedInstanceState: Bundle?) {
        val cards = FeaturedContent.from(DemoVideoCatalog.items)
        binding.featuredList.layoutManager = LinearLayoutManager(requireContext())
        binding.featuredList.adapter = FeaturedVideoAdapter(cards) { card ->
            parentFragmentManager.setFragmentResult(
                REQUEST_OPEN_FEED,
                bundleOf(KEY_MEDIA_ID to card.id),
            )
        }
    }

    override fun onBindingDestroyed(binding: FeaturedFragmentBinding) {
        binding.featuredList.adapter = null
    }

    companion object {
        const val REQUEST_OPEN_FEED = "featured:open-feed"
        const val KEY_MEDIA_ID = "mediaId"
    }
}
