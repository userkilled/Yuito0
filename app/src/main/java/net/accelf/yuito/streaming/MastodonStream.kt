package net.accelf.yuito.streaming

import android.util.Log
import com.google.gson.Gson
import com.keylesspalace.tusky.appstore.EventHub
import com.keylesspalace.tusky.appstore.PreferenceChangedEvent
import com.keylesspalace.tusky.appstore.StatusDeletedEvent
import com.keylesspalace.tusky.appstore.StreamUpdateEvent
import com.keylesspalace.tusky.entity.Filter
import com.keylesspalace.tusky.entity.Status
import com.keylesspalace.tusky.network.MastodonApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.accelf.yuito.streaming.SubscribeRequest.RequestType.SUBSCRIBE
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MastodonStream(
    coroutineScope: CoroutineScope,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val eventHub: EventHub,
) : WebSocketListener(), CoroutineScope by coroutineScope {

    private var webSocket: WebSocket? = null

    fun openSocket(subscriptions: Set<Subscription>) {
        val request = Request.Builder().url(STREAMING_URL).build()
        webSocket = okHttpClient.newWebSocket(request, this)
        subscriptions.forEach {
            send(SubscribeRequest.fromSubscription(SUBSCRIBE, it))
            Log.d(TAG, "Subscribed $it")
        }
    }

    fun closeSocket() {
        webSocket!!.close(1000, null)
        webSocket = null
    }

    private fun send(subscribeRequest: SubscribeRequest) {
        webSocket!!.send(gson.toJson(subscribeRequest))
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "Stream connected")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val event = gson.fromJson(text, StreamEvent::class.java)
        val payload = event.payload
        when (event.event) {
            StreamEvent.EventType.UPDATE -> {
                val status = gson.fromJson(payload, Status::class.java)
                launch {
                    eventHub.dispatch(StreamUpdateEvent(status, Subscription.fromStreamList(event.stream), this@MastodonStream.hashCode()))
                }
            }
            StreamEvent.EventType.DELETE -> launch {
                eventHub.dispatch(StatusDeletedEvent(payload))
            }
            StreamEvent.EventType.FILTERS_CHANGED -> launch {
                eventHub.dispatch(PreferenceChangedEvent(Filter.Kind.HOME.kind)) // It may be not a home but it doesn't matter
            }
            else -> Log.d(TAG, "Unsupported event type.")
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "Stream closed")
    }

    companion object {
        private const val TAG = "MastodonStream"

        private val STREAMING_URL by lazy {
            HttpUrl.Builder()
                .scheme("https")
                .host(MastodonApi.PLACEHOLDER_DOMAIN)
                .addPathSegments("api/v1/streaming")
                .build()
                .toString()
                .replace("https://", "wss://")
        }
    }
}
