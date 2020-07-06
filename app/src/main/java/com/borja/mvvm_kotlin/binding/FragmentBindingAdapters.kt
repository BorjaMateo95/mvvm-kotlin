package com.borja.mvvm_kotlin.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import javax.inject.Inject

class FragmentBindingAdapters @Inject constructor(val fragment: Fragment){

    @BindingAdapter("imagenUrl")
    fun bindImage(imageView: ImageView, url: String){
        Glide.with(fragment).load(url).into(imageView)
    }
}