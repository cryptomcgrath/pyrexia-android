package com.cryptomcgrath.pyrexia.util

import androidx.databinding.BindingAdapter
import com.facebook.shimmer.ShimmerFrameLayout

@BindingAdapter("loading")
fun ShimmerFrameLayout.setLoading(isLoading: Boolean) {
    if (isLoading) {
        this.showShimmer(true)
    } else {
        this.hideShimmer()
    }
}