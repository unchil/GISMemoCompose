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

@OptIn(ExperimentalCoilApi::class)
fun KtorNetworkFetcherFactory() = NetworkFetcher.Factory(
    networkClient = { HttpClient().asNetworkClient() }
)

class GisMemo : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components{
                add(factory = KtorNetworkFetcherFactory())
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