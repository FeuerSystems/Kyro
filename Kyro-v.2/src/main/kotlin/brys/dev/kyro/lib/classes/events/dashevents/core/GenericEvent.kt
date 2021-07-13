package brys.dev.kyro.lib.classes.events.dashevents.core

import java.util.*

interface GenericEvent: EventListener {
    fun onEvent()
}