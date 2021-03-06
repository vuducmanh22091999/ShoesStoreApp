package com.example.movieapp.ui.add

import android.net.Uri
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_image.*

class ListImageFragment(private var urlImage: String): BaseFragment() {
    override fun getLayoutID(): Int {
        return R.layout.fragment_image
    }

    override fun doViewCreated() {
        context?.let {
            Glide.with(it).load(Uri.parse(urlImage)).into(image)
        }
    }
}