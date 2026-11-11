package com.example.belltest.message

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.belltest.base.BaseViewBindingFragment
import com.example.belltest.message.databinding.MessageFragmentBinding

class MessageFragment : BaseViewBindingFragment<MessageFragmentBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): MessageFragmentBinding =
        MessageFragmentBinding.inflate(inflater, container, false)
}
