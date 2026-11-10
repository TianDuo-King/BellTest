package com.example.belltest.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.belltest.base.applySystemBarsPadding
import com.example.belltest.message.databinding.MessageFragmentBinding

class MessageFragment : Fragment() {
    private var _binding: MessageFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = MessageFragmentBinding.inflate(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        requireNotNull(_binding).messageRoot.applySystemBarsPadding(includeTop = true)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
