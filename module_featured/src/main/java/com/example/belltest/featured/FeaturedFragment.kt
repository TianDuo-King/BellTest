package com.example.belltest.featured

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belltest.base.applySystemBarsPadding
import com.example.belltest.featured.databinding.FeaturedFragmentBinding
import com.example.belltest.model.DemoVideoCatalog

class FeaturedFragment : Fragment() {
    private var _binding: FeaturedFragmentBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FeaturedFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.featuredRoot.applySystemBarsPadding(includeTop = true)
        val cards = FeaturedContent.from(DemoVideoCatalog.items)
        binding.featuredList.layoutManager = LinearLayoutManager(requireContext())
        binding.featuredList.adapter = FeaturedVideoAdapter(cards) { card ->
            parentFragmentManager.setFragmentResult(
                REQUEST_OPEN_FEED,
                bundleOf(KEY_MEDIA_ID to card.id),
            )
        }
    }

    override fun onDestroyView() {
        binding.featuredList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_OPEN_FEED = "featured:open-feed"
        const val KEY_MEDIA_ID = "mediaId"
    }
}
