package brys.dev.kyro.lib.classes.events.dashevents.core

import brys.dev.kyro.lib.classes.events.dashevents.events.TestEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.TrackChangeEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.UserJoinedChannelEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.UserLeftChannelEvent
import com.google.common.eventbus.Subscribe

interface GenericEventListener {
    fun onTestEvent(event: TestEvent)
    fun onUserJoinEvent(event: UserJoinedChannelEvent)
    fun onUserLeftEvent(event: UserLeftChannelEvent)
    fun onTrackChangeEvent(event: TrackChangeEvent)
}