package brys.dev.kyro.lib.classes.events.dashevents.core

import brys.dev.kyro.lib.classes.events.dashevents.events.TestEvent
import brys.dev.kyro.unsafe.EXEventBus
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe





class WSEvents(private val bus: EXEventBus) {
    suspend fun post(event: Any) {
        bus.send(event)
    }
}