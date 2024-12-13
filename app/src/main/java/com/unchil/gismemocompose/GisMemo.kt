package com.unchil.gismemocompose

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.memory.MemoryCache
import coil3.network.NetworkFetcher
import coil3.network.ktor3.asNetworkClient
import coil3.request.crossfade
import coil3.util.DebugLogger
import io.ktor.client.HttpClient



class GisMemo : Application(), SingletonImageLoader.Factory {

    val KtorNetwork = { HttpClient().asNetworkClient() }

    @OptIn(ExperimentalCoilApi::class)
    val NetworkFetcherFactory = NetworkFetcher.Factory(
        networkClient = KtorNetwork
    )

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true) // 이미지가 로드되면서 서서히 보여지는 효과 적용
            .components{
                add(factory = NetworkFetcherFactory)
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent( context,0.25)
                    .build()
            }.apply {
                logger(DebugLogger())
            }.build()
    }
}