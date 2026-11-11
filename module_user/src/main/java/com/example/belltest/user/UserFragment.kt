package com.example.belltest.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.belltest.base.BaseViewBindingFragment
import com.example.belltest.lab.AsyncComparisonActivity
import com.example.belltest.lab.LeakDemoActivity
import com.example.belltest.lab.RxLearningActivity
import com.example.belltest.user.databinding.UserFragmentBinding

class UserFragment : BaseViewBindingFragment<UserFragmentBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): UserFragmentBinding =
        UserFragmentBinding.inflate(inflater, container, false)

    override fun onBindingCreated(binding: UserFragmentBinding, savedInstanceState: Bundle?) {
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
}
