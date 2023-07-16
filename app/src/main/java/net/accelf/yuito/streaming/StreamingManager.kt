package net.accelf.yuito.streaming

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.keylesspalace.tusky.appstore.EventHub
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingManager @Inject constructor(
    private val eventHub: EventHub,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {

    private var stream: MastodonStream? = null

    fun setup(owner: LifecycleOwner, subscriptions: Set<Subscription>, onStatusChange: (Boolean) -> Unit) {
        owner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    stream = MastodonStream(owner.lifecycleScope, okHttpClient, gson, eventHub)
                    stream?.openSocket(subscriptions)
                    onStatusChange(true)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    stream?.closeSocket()
                    stream = null
                    onStatusChange(false)
                }
                else -> {}
            }
        })
    }
}
