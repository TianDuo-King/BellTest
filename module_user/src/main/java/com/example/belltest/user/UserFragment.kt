package com.example.belltest.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.belltest.base.applySystemBarsPadding
import com.example.belltest.lab.AsyncComparisonActivity
import com.example.belltest.lab.LeakDemoActivity
import com.example.belltest.lab.RxLearningActivity
import com.example.belltest.user.databinding.UserFragmentBinding

class UserFragment : Fragment() {
    private var _binding: UserFragmentBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = UserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.userRoot.applySystemBarsPadding(includeTop = true)
        binding.openAsyncLab.setOnClickListener {
            startActivity(Intent(requireContext(), AsyncComparisonActivity::class.java))
        }
        binding.openRxLab.setOnClickListener {
            startActivity(Intent(requireContext(), RxLearningActivity::class.java))
        }
        binding.openLeakLab.setOnClickListener {
            startActivity(Intent(requireContext(), LeakDemoActivity::class.java))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
