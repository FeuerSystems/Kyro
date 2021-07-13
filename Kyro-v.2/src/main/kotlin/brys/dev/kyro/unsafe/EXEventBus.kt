package brys.dev.kyro.unsafe

import brys.dev.kyro.unsafe.EXEventBus.bus
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


object EXEventBus {
    @OptIn(ExperimentalCoroutinesApi::class)
    val bus: BroadcastChannel<Any> = BroadcastChannel(DEFAULT_BUFFER_SIZE)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun send(o: Any) {
            bus.send(o)
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend inline fun <reified T> asChannel(): Flow<T> {
        return bus.openSubscription().consumeAsFlow().map { it as T }
    }
    }


