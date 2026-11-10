package com.example.belltest.publish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.belltest.base.applySystemBarsPadding
import com.example.belltest.publish.databinding.PublishFragmentBinding

class PublishFragment : Fragment() {
    private var _binding: PublishFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = PublishFragmentBinding.inflate(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        requireNotNull(_binding).publishRoot.applySystemBarsPadding(includeTop = true)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
