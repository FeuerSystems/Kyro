package brys.dev.kyro.lib.classes.events.dashevents.core

import brys.dev.kyro.lib.classes.events.dashevents.events.TestEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.TrackChangeEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.UserJoinedChannelEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.UserLeftChannelEvent

 abstract class GenericListener: GenericEventListener {
    override fun onTestEvent(event: TestEvent) {
        TODO("Not yet implemented")
    }

    override fun onUserJoinEvent(event: UserJoinedChannelEvent) {
        TODO("Not yet implemented")
    }

    override fun onUserLeftEvent(event: UserLeftChannelEvent) {
        TODO("Not yet implemented")
    }

    override fun onTrackChangeEvent(event: TrackChangeEvent) {
        TODO("Not yet implemented")
    }
}