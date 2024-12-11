package com.unchil.gismemocompose

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.unchil.gismemocompose.api.UnsplashSizingInterceptor


class GisMemo : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(UnsplashSizingInterceptor)
            }
            .build()
    }
}