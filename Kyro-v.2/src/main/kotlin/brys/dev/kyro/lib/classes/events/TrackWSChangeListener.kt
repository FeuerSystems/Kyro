package brys.dev.kyro.lib.classes.events

import brys.dev.kyro.lib.interfaces.TrackWSChangeEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import net.dv8tion.jda.api.entities.User


class TrackWSChangeListener(private val router: Routing): TrackWSChangeEvent {
    override fun track(before: AudioTrack?, after: AudioTrack?) {
        val validBefore =
            if (before == null) "null" else "{\"name\": \"${before.info?.title}\", \"author\": \"${before.info?.author}\", \"stream\": ${before.info.isStream}, \"id\": \"${before.info?.identifier}\", \"duration\": ${before.duration}, \"position\": ${before.position}, \"user\": ${before.userData as User}}"
        val validAfter =
            if (after == null) "null" else "{\"name\": \"${after.info?.title}\", \"author\": \"${after.info?.author}\", \"stream\": ${after.info.isStream}, \"id\": \"${after.info?.identifier}\", \"duration\": ${after.duration}, \"position\": ${after.position}, \"user\": ${after.userData as User}}"
        router {
            webSocket("/ws") {
                send(Frame.Text("{ \"type\": \"change_track_event\", \"before\": [${validBefore}], \"after\": [${validAfter}]  }"))
            }
        }
    }

}