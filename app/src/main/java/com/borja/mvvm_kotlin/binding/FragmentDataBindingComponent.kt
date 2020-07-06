package com.borja.mvvm_kotlin.binding

import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingComponent

class FragmentDataBindingComponent(fragment: Fragment) : DataBindingComponent {

    private val adapter = FragmentBindingAdapters(fragment)

    override fun getFragmentBindingAdapters(): FragmentBindingAdapters = adapter
}